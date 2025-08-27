package com.hibernate.gymapp;

import com.hibernate.gymapp.utils.HibernateUtil;
import org.hibernate.Session;

public class App {
    public static void main(String[] args) {

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.getTransaction().commit();
            System.out.println("Hibernate setup works!");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            HibernateUtil.shutdown();
        }
    }
}
