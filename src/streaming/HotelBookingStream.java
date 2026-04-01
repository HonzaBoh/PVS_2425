package streaming;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class HotelBookingStream {
    public static void main(String[] args) {
        //URL: https://forms.cloud.microsoft/Pages/ResponsePage.aspx?id=f_BDFfkwekCetYp67EGP_1pSNPXTVBFPg7PGychWGEdUQzg5VTZZREVXUlZZNUpaRzk3N0RaU0FXVCQlQCNjPTEu
        List<HotelBooking> bookings = HotelBooking.generateSampleBookings(100);
        bookings.forEach(System.out::println);

        //Vsechny bookings pres webovky
        List<HotelBooking> webBookings = bookings.stream().filter(b -> b.bookingChannel.equals("Website"));

        //vypiste prumernou cenu za booking u 'G002'
        Double avgPrice = (Double) bookings.stream().filter(b -> b.guestId.equals("G002")).mapToDouble(HotelBooking::getPrice).average().orElse(0);

        //vypiste bookingy pres telefon serazene dle ceny
        List<HotelBooking> telBookings = bookings.stream().filter(b -> b.bookingChannel.equals("Phone")).sorted((b1,b2) -> b1.price.compareTo(b2.price));
        telBookings.forEach(System::println);

        //Mapa bookingu dle mest
        Map<String, HotelBooking> cityGroups = bookings.stream().collect(Collectors.groupingBy(HotelBooking::getCity));

        //Mapa Status: pocet objednavek u kazdeho statustu (Mapa<Status, count>
        Map<String, int> statuses = bookings.stream().collect(Collectors.groupingBy(HotelBooking::getStatus), Collectors.counting());
        
    }
}

class HotelBooking {
    private String bookingId;
    private String guestId;
    private String city;
    private String roomType;
    private double price;
    private LocalDate checkInDate;
    private int nights;
    private String bookingChannel;
    private String status;

    public HotelBooking(String bookingId, String guestId, String city, String roomType,
                        double price, LocalDate checkInDate, int nights,
                        String bookingChannel, String status) {
        this.bookingId = bookingId;
        this.guestId = guestId;
        this.city = city;
        this.roomType = roomType;
        this.price = price;
        this.checkInDate = checkInDate;
        this.nights = nights;
        this.bookingChannel = bookingChannel;
        this.status = status;
    }

    public String getBookingId() { return bookingId; }
    public String getGuestId() { return guestId; }
    public String getCity() { return city; }
    public String getRoomType() { return roomType; }
    public double getPrice() { return price; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public int getNights() { return nights; }
    public String getBookingChannel() { return bookingChannel; }
    public String getStatus() { return status; }

    @Override
    public String toString() {
        return String.format(
                "HotelBooking{ID=%s, Guest=%s, City=%s, Room=%s, Price=%.2f, CheckIn=%s, Nights=%d, Channel=%s, Status=%s}",
                bookingId, guestId, city, roomType, price, checkInDate, nights, bookingChannel, status
        );
    }

    public static List<HotelBooking> generateSampleBookings(int count) {
        Random random = new Random();

        List<String> guests = Arrays.asList("G001", "G002", "G003", "G004", "G005", "G006");
        List<String> cities = Arrays.asList("Prague", "Brno", "Vienna", "Berlin", "Budapest");
        List<String> roomTypes = Arrays.asList("Single", "Double", "Suite", "Deluxe");
        List<String> channels = Arrays.asList("Website", "Booking.com", "Phone", "Travel Agency");
        List<String> statuses = Arrays.asList("Confirmed", "Cancelled", "Completed", "Pending");

        return IntStream.range(0, count)
                .mapToObj(i -> new HotelBooking(
                        "HB" + (1000 + i),
                        guests.get(random.nextInt(guests.size())),
                        cities.get(random.nextInt(cities.size())),
                        roomTypes.get(random.nextInt(roomTypes.size())),
                        80 + (420 * random.nextDouble()),
                        LocalDate.now().plusDays(random.nextInt(90) - 30), // some past, some future
                        1 + random.nextInt(7),
                        channels.get(random.nextInt(channels.size())),
                        statuses.get(random.nextInt(statuses.size()))
                ))
                .collect(Collectors.toList());
    }
}
