import com.hibernate.gymapp.model.Trainee;
import com.hibernate.gymapp.model.User;
import com.hibernate.gymapp.repository.TraineeRepository;
import com.hibernate.gymapp.repository.TrainerRepository;
import com.hibernate.gymapp.repository.TrainingRepository;
import com.hibernate.gymapp.repository.UserRepository;
import com.hibernate.gymapp.service.AuthenticationService;
import com.hibernate.gymapp.service.TraineeService;
import com.hibernate.gymapp.utils.CredentialsGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TraineeServiceTest {

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private CredentialsGenerator credentialsGenerator;

    @Mock
    private TrainingRepository trainingRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @InjectMocks
    private TraineeService traineeService;


    @Test
    void createTraineeProfile_Success() {

        String firstName = "John";
        String lastName = "Doe";
        LocalDate dob = LocalDate.of(2000, 1, 1);
        String username = "johndoe";
        String password = "pass123";

        when(credentialsGenerator.generateUsername(firstName, lastName, userRepository))
                .thenReturn(username);
        when(credentialsGenerator.generatePassword())
                .thenReturn(password);

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User u = invocation.getArgument(0);
                    u.setId(1L);
                    return Optional.of(u);
                });

        when(traineeRepository.save(any(Trainee.class)))
                .thenAnswer(invocation -> {
                    Trainee t = invocation.getArgument(0);
                    t.setId(1L);
                    return Optional.of(t);
                });

        Optional<Trainee> result = traineeService.createTraineeProfile(firstName, lastName, dob, "123 Street");

        assertTrue(result.isPresent());
        assertEquals(username, result.get().getUser().getUsername());
        assertEquals(password, result.get().getUser().getPassword());
    }

    @Test
    void createTraineeProfile_InvalidName() {
        Optional<Trainee> result = traineeService.createTraineeProfile("", "", null, null);
        assertTrue(!result.isPresent());
    }

    @Test
    void getTraineeProfileByUsername_Success() {
        User user = new User();
        user.setUsername("john");
        Trainee trainee = new Trainee();
        trainee.setUser(user);

        when(authenticationService.authenticateTrainee("john", "pass")).thenReturn(true);
        when(traineeRepository.findByUsername("john")).thenReturn(Optional.of(trainee));

        Optional<Trainee> result = traineeService.getTraineeProfileByUsername("john", "pass");
        assertTrue(result.isPresent());
        assertEquals(user, result.get().getUser());
    }

    @Test
    void getTraineeProfileByUsername_FailedAuthentication() {
        when(authenticationService.authenticateTrainee("john", "pass")).thenReturn(false);
        Optional<Trainee> result = traineeService.getTraineeProfileByUsername("john", "pass");
        assertTrue(!result.isPresent());
    }

    @Test
    void changeTraineePassword_Success() {
        User user = new User();
        user.setUsername("john");
        user.setPassword("oldPass");
        user.setIsActive(true);

        Trainee trainee = new Trainee();
        trainee.setUser(user);

        when(traineeRepository.findByUsername("john")).thenReturn(Optional.of(trainee));
        when(authenticationService.authenticateTrainee("john", "oldPass")).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        boolean changed = traineeService.changeTraineePassword("john", "oldPass", "newPass");

        assertTrue(changed);
        assertEquals("newPass", trainee.getUser().getPassword());
    }

    @Test
    void changeTraineePassword_FailedAuthentication() {
        when(authenticationService.authenticateTrainee("john", "wrong")).thenReturn(false);
        boolean changed = traineeService.changeTraineePassword("john", "wrong", "newPass");
        assertFalse(changed);
    }

    @Test
    void changeTraineePassword_EmptyNewPassword() {
        User user = new User();
        user.setUsername("john");
        user.setPassword("oldPass");
        user.setIsActive(true);
        Trainee trainee = new Trainee();
        trainee.setUser(user);

        when(traineeRepository.findByUsername("john")).thenReturn(Optional.of(trainee));
        when(authenticationService.authenticateTrainee("john", "oldPass")).thenReturn(true);

        boolean changed = traineeService.changeTraineePassword("john", "oldPass", "");
        assertFalse(changed);
    }

    @Test
    void activateDeactivateTrainee_Success() {
        User user = new User();
        user.setUsername("john");
        user.setIsActive(false);
        Trainee trainee = new Trainee();
        trainee.setUser(user);

        when(authenticationService.authenticateTrainee("john", "pass")).thenReturn(true);
        when(traineeRepository.findByUsername("john")).thenReturn(Optional.of(trainee));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        boolean result = traineeService.activateDeactivateTrainee("john", "pass", true);
        assertTrue(result);
        assertTrue(trainee.getUser().getIsActive());
    }

    @Test
    void activateDeactivateTrainee_AlreadyActive() {
        User user = new User();
        user.setUsername("john");
        user.setIsActive(true);
        Trainee trainee = new Trainee();
        trainee.setUser(user);

        when(authenticationService.authenticateTrainee("john", "pass")).thenReturn(true);
        when(traineeRepository.findByUsername("john")).thenReturn(Optional.of(trainee));

        boolean result = traineeService.activateDeactivateTrainee("john", "pass", true);
        assertFalse(result);
    }

    @Test
    void updateTraineeProfile_Success() {
        User user = new User();
        user.setUsername("john");
        user.setFirstName("John");
        user.setLastName("Doe");
        Trainee trainee = new Trainee();
        trainee.setUser(user);

        when(authenticationService.authenticateTrainee("john", "pass")).thenReturn(true);
        when(traineeRepository.findByUsername("john")).thenReturn(Optional.of(trainee));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(traineeRepository.save(any(Trainee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Trainee> result = traineeService.updateTraineeProfile("john", "pass",
                "Johnny", "Doey", LocalDate.of(2000,1,1), "Address", true);

        assertTrue(result.isPresent());
        assertEquals("Johnny", result.get().getUser().getFirstName());
        assertEquals("Doey", result.get().getUser().getLastName());
    }

    @Test
    void deleteTraineeProfile_Success() {
        User user = new User();
        user.setUsername("john");
        Trainee trainee = new Trainee();
        trainee.setUser(user);

        when(authenticationService.authenticateTrainee("john", "pass")).thenReturn(true);
        when(traineeRepository.findByUsername("john")).thenReturn(Optional.of(trainee));

        boolean deleted = traineeService.deleteTraineeProfile("john", "pass");
        assertTrue(deleted);
        verify(traineeRepository, times(1)).delete(trainee);
    }

}
