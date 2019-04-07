package services;

import database.Database;
import models.*;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DateTimeException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class ReservationServiceTest {
    private Database database;
    private ReservationService reservationService;
    private RoomService roomService;
    private UserService userService;
    private HotelService hotelService;
    private Reservation reservation;
    private Reservation reservation2;
    private Room room;
    private User user;
    private User user2;
    private User user3;
    private Hotel hotel;

    @BeforeEach
    public void init() {
        database = new Database();
        reservationService = new ReservationService();
        roomService = new RoomService();
        userService = new UserService();
        hotelService = new HotelService();
        hotel = new Hotel("Sample name", new LocalTime(8), new LocalTime(23));
        room = new Room(hotel, 200, 2);
        user = new User("test@test.com");
        user2 = new User("test2@test2.com");
        user3 = new User("test3@test3.com");
        reservation = new Reservation(new DateTime(2019, 5, 5, 11, 0),
                new DateTime(2019, 5, 6, 11, 0),
                user, room);
        reservation2 = new Reservation(new DateTime(2019, 6, 6, 11, 0),
                new DateTime(2019, 6, 9, 11, 0),
                user2, room);
        hotelService.addHotelToDatabase(database, hotel);
        roomService.addRoomToDatabase(database, room);
        userService.addUserToDatabase(database, user);
        userService.addUserToDatabase(database, user2);
        userService.addUserToDatabase(database, user3);
    }

    @Test
    @DisplayName("validation of reservation (valid)")
    public void reservationValidationTest() {
        boolean result = reservationService.reservationValidation(reservation);
        assertTrue(result);
    }

    @Test
    @DisplayName("validation of reservation " +
            "(returns false because of null argument)")
    public void reservationValidation2Test() {
        assertFalse(reservationService.reservationValidation(null));
    }

    @Test
    @DisplayName("validation of reservation " +
            "(returns false because start date is after end date)")
    public void reservationValidation3Test() {
        reservation.setEndDate(new DateTime(2019, 1, 1, 11, 0));
        assertFalse(reservationService.reservationValidation(reservation));
    }

    @Test
    @DisplayName("validation of reservation " +
            "(returns false because user in reservation is null)")
    public void reservationValidation4Test() {
        reservation.setUser(null);
        assertFalse(reservationService.reservationValidation(reservation));
    }

    @Test
    @DisplayName("validation of reservation " +
            "(returns false because room in reservation is null)")
    public void reservationValidation5Test() {
        reservation.setRoom(null);
        assertFalse(reservationService.reservationValidation(reservation));
    }

    @Test
    @DisplayName("validation of reservation " +
            "(returns false because user and room in reservation is null)")
    public void reservationValidation6Test() {
        reservation.setUser(null);
        reservation.setRoom(null);
        assertFalse(reservationService.reservationValidation(reservation));
    }

    @Test
    @DisplayName("adding reservation to database (valid)")
    public void addReservationToDatabaseTest() {
        assertTrue(database.getReservations().isEmpty());
        reservationService.addReservationToDatabase(database, reservation);
        reservationService.addReservationToDatabase(database, reservation2);

        HashMap<Integer, Reservation> reservationsTemp = new HashMap<>();
        reservationsTemp.put(1, reservation);
        reservationsTemp.put(2, reservation2);
        assertEquals(reservationsTemp, database.getReservations());
    }

    @Test
    @DisplayName("adding reservation to database " +
            "(throws NullPointerException when database is null")
    public void addReservationToDatabase2Test() {
        assertThrows(NullPointerException.class,
                () -> reservationService.addReservationToDatabase(null, reservation));
    }

    @Test
    @DisplayName("adding reservation to database " +
            "(throws IllegalArgumentException when reservation is null")
    public void addReservationToDatabase3Test() {
        assertThrows(IllegalArgumentException.class,
                () -> reservationService.addReservationToDatabase(database, null));
    }

    @Test
    @DisplayName("adding reservation to database " +
            "(throws IllegalArgumentException when database and reservation are null")
    public void addReservationToDatabase4Test() {
        assertThrows(IllegalArgumentException.class,
                () -> reservationService.addReservationToDatabase(null, null));
    }

    @Test
    @DisplayName("adding reservation to database" +
            "(throws IllegalArgumentException when reservation doesn't pass validation")
    public void addReservationToDatabase5Test() {
        reservation.setEndDate(null);
        assertThrows(IllegalArgumentException.class,
                () -> reservationService.addReservationToDatabase(database, reservation));
    }

    @Test
    @DisplayName("adding reservation to database" +
            "(throws NullPointerException when room doesn't exist in database")
    public void addReservationToDatabase6Test() {
        reservation.setRoom(new Room(hotel, 666, 1));
        assertThrows(NullPointerException.class,
                () -> reservationService.addReservationToDatabase(database, reservation));
    }

    @Test
    @DisplayName("adding reservation to database" +
            "(throws NullPointerException when user doesn't exist in database")
    public void addReservationToDatabase7Test() {
        reservation.setUser(new User("example@example.pl"));
        assertThrows(NullPointerException.class,
                () -> reservationService.addReservationToDatabase(database, reservation));
    }

    @Test
    @DisplayName("adding reservation to database " +
            "(throws DateTimeException because selected (exactly the same) date is reserved by other person)")
    public void addReservationToDatabase8Test() {
        reservationService.addReservationToDatabase(database, reservation);
        reservationService.addReservationToDatabase(database, reservation2);
        Reservation newReservation = new Reservation(
                new DateTime(2019, 5, 5, 11, 0),
                new DateTime(2019, 5, 6, 11, 0),
                user3, room);

        assertThrows(DateTimeException.class,
                () -> reservationService.addReservationToDatabase(database, newReservation));
    }

    @Test
    @DisplayName("adding reservation to database " +
            "(throws DateTimeException because selected date (that covers other date) " +
            "is reserved by other person)")
    public void addReservationToDatabase9Test() {
        reservationService.addReservationToDatabase(database, reservation);
        reservationService.addReservationToDatabase(database, reservation2);
        Reservation newReservation = new Reservation(
                new DateTime(2019, 5, 6, 10, 0),
                new DateTime(2019, 5, 7, 10, 0),
                user3, room);

        assertThrows(DateTimeException.class,
                () -> reservationService.addReservationToDatabase(database, newReservation));
    }


    @Test
    @DisplayName("getting reservations of specific user when hashmap of user's revervations is not empty")
    public void getReservationsOfUserTest() {
        HashMap<Integer, Reservation> reservations = new HashMap<>();
        reservations.put(1, reservation);
        reservationService.addReservationToDatabase(database, reservation);
        reservationService.addReservationToDatabase(database, reservation2);

        assertEquals(reservations, reservationService.getReservationsOfUser(database, user));
    }

    @Test
    @DisplayName("getting reservations of specific user when hashmap of user's revervation is empty")
    public void getReservationsOfUser2Test() {
        assertTrue(reservationService.getReservationsOfUser(database, user2).isEmpty());
    }

    @Test
    @DisplayName("getting reservations of specific user" +
            "(throws IllegalArgumentException when database is null")
    public void getReservationsOfUser3Test() {
        assertThrows(NullPointerException.class,
                () -> reservationService.getReservationsOfUser(null, user));
    }

    @Test
    @DisplayName("getting reservations of specific user" +
            "(throws IllegalArgumentException when user is null")
    public void getReservationsOfUser4Test() {
        assertThrows(NullPointerException.class,
                () -> reservationService.getReservationsOfUser(database, null));
    }

    @Test
    @DisplayName("getting reservations of specific user" +
            "(throws IllegalArgumentException when database and user are null")
    public void getReservationsOfUser5Test() {
        assertThrows(NullPointerException.class,
                () -> reservationService.getReservationsOfUser(null, null));
    }

    @AfterEach
    public void cleanup() {
        database = null;
        reservationService = null;
        userService = null;
        hotelService = null;
        roomService = null;
        reservation = null;
        reservation2 = null;
        room = null;
        user = null;
        user2 = null;
        user3 = null;
        hotel = null;
    }
}
