import com.hibernate.gymapp.model.Trainee;
import com.hibernate.gymapp.model.Trainer;
import com.hibernate.gymapp.model.TrainingType;
import com.hibernate.gymapp.model.User;
import com.hibernate.gymapp.repository.TraineeRepository;
import com.hibernate.gymapp.repository.TrainerRepository;
import com.hibernate.gymapp.repository.TrainingRepository;
import com.hibernate.gymapp.repository.UserRepository;
import com.hibernate.gymapp.service.AuthenticationService;
import com.hibernate.gymapp.service.TrainerService;
import com.hibernate.gymapp.utils.CredentialsGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TrainerServiceTest {

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private CredentialsGenerator credentialsGenerator;

    @Mock
    private TrainingRepository trainingRepository;

    @Mock
    private TraineeRepository traineeRepository;

    @InjectMocks
    private TrainerService trainerService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createTrainerProfile_Success() {

        String firstName = "John";
        String lastName = "Doe";
        TrainingType specialization = new TrainingType("random");

        String username = "johndoe";
        String password = "pass123";

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername(username);
        mockUser.setPassword(password);

        Trainer mockTrainer = new Trainer();
        mockTrainer.setId(1L);
        mockTrainer.setUser(mockUser);

        when(credentialsGenerator.generateUsername(eq(firstName), eq(lastName), any(UserRepository.class)))
                .thenReturn(username);
        when(credentialsGenerator.generatePassword()).thenReturn(password);

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(trainerRepository.save(any(Trainer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Trainer> result = trainerService.createTrainerProfile(firstName, lastName, specialization);

        assertTrue(result.isPresent());
        assertEquals(username, result.get().getUser().getUsername());
        assertEquals(password, result.get().getUser().getPassword());
    }

    @Test
    void createTrainerProfile_InvalidInput() {
        Optional<Trainer> result = trainerService.createTrainerProfile("", "", null);
        assertFalse(result.isPresent());
        verifyNoInteractions(userRepository, trainerRepository);
    }

    @Test
    void getTrainerProfileByUsername_Success() {

        String username = "trainer1";
        String password = "pass";
        Trainer trainer = new Trainer();

        when(authenticationService.authenticateTrainer(username, password)).thenReturn(true);
        when(trainerRepository.findByUsername(username)).thenReturn(Optional.of(trainer));

        Optional<Trainer> result = trainerService.getTrainerProfileByUsername("john", "pass");

        assertTrue(result.isPresent());
        verify(trainerRepository).findByUsername(username);
    }

    @Test
    void getTrainerProfileByUsername_FailedAuthentication() {
        String username = "trainer1";
        String password = "wrong";
        when(authenticationService.authenticateTrainer(username, password)).thenReturn(false);

        Optional<Trainer> result = trainerService.getTrainerProfileByUsername(username, password);

        assertFalse(result.isPresent());
        verifyNoInteractions(trainerRepository);
    }

    @Test
    void changeTrainerPassword_Success() {
        User user = new User();
        user.setUsername("john");
        user.setPassword("oldPass");
        user.setIsActive(true);

        Trainer trainer = new Trainer();
        trainer.setUser(user);

        when(trainerRepository.findByUsername("john")).thenReturn(Optional.of(trainer));
        when(authenticationService.authenticateTrainer("john", "oldPass")).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        boolean changed = trainerService.changeTrainerPassword("john", "oldPass", "newPass");

        assertTrue(changed);
        assertEquals("newPass", trainer.getUser().getPassword());
    }

    @Test
    void changeTrainerPassword_FailedAuthentication() {
        when(authenticationService.authenticateTrainee("john", "wrong")).thenReturn(false);
        boolean changed = trainerService.changeTrainerPassword("john", "wrong", "newPass");
        assertFalse(changed);
    }

    @Test
    void changeTrainerPassword_EmptyNewPassword() {
        User user = new User();
        user.setUsername("john");
        user.setPassword("oldPass");
        user.setIsActive(true);
        Trainee trainee = new Trainee();
        trainee.setUser(user);

        when(traineeRepository.findByUsername("john")).thenReturn(Optional.of(trainee));
        when(authenticationService.authenticateTrainee("john", "oldPass")).thenReturn(true);

        boolean changed = trainerService.changeTrainerPassword("john", "oldPass", "");
        assertFalse(changed);
    }

    @Test
    void activateDeactivateTrainer_Success() {
        User user = new User();
        user.setUsername("john");
        user.setIsActive(false);
        Trainer trainer = new Trainer();
        trainer.setUser(user);

        when(authenticationService.authenticateTrainer("john", "pass")).thenReturn(true);
        when(trainerRepository.findByUsername("john")).thenReturn(Optional.of(trainer));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        boolean result = trainerService.activateDeactivateTrainer("john", "pass", true);
        assertTrue(result);
        assertTrue(trainer.getUser().getIsActive());
    }

    @Test
    void activateDeactivateTrainer_AlreadyActive() {
        User user = new User();
        user.setUsername("john");
        user.setIsActive(true);
        Trainer trainer = new Trainer();
        trainer.setUser(user);

        when(authenticationService.authenticateTrainer("john", "pass")).thenReturn(true);
        when(trainerRepository.findByUsername("john")).thenReturn(Optional.of(trainer));

        boolean result = trainerService.activateDeactivateTrainer("john", "pass", true);
        assertFalse(result);
    }

    @Test
    void updateTrainerProfile_Success() {
        User user = new User();
        user.setUsername("john");
        user.setFirstName("John");
        user.setLastName("Doe");
        Trainer trainer = new Trainer();
        trainer.setUser(user);

        when(authenticationService.authenticateTrainee("john", "pass")).thenReturn(true);
        when(trainerRepository.findByUsername("john")).thenReturn(Optional.of(trainer));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(trainerRepository.save(any(Trainer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Trainer> result = trainerService.updateTrainerProfile("john", "pass",
                "Johnny", "Doey", new TrainingType("newtype"), true);

        assertTrue(result.isPresent());
        assertEquals("Johnny", result.get().getUser().getFirstName());
        assertEquals("Doey", result.get().getUser().getLastName());
    }

    @Test
    void deleteTrainerProfile_Success() {
        User user = new User();
        user.setUsername("john");
        Trainer trainer = new Trainer();
        trainer.setUser(user);

        when(authenticationService.authenticateTrainee("john", "pass")).thenReturn(true);
        when(trainerRepository.findByUsername("john")).thenReturn(Optional.of(trainer));

        boolean deleted = trainerService.deleteTrainerProfile("john", "pass");
        assertTrue(deleted);
        verify(trainerRepository, times(1)).delete(trainer);
    }
}

