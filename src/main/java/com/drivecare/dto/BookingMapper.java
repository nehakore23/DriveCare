package com.drivecare.dto;

import com.drivecare.entity.Booking;
import com.drivecare.entity.BookingStatus;

public final class BookingMapper {

    private BookingMapper() {
    }

    public static BookingResponse toResponse(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        if (booking.getCustomer() != null) {
            response.setCustomerId(booking.getCustomer().getId());
            response.setCustomerName(booking.getCustomer().getName());
            response.setCustomerPhone(booking.getCustomer().getPhone());
        }
        if (booking.getVehicle() != null) {
            response.setVehicleId(booking.getVehicle().getId());
            response.setVehicleNumber(booking.getVehicle().getVehicleNumber());
            response.setVehicleBrand(booking.getVehicle().getBrand());
            response.setVehicleModel(booking.getVehicle().getModel());
        }
        if (booking.getService() != null) {
            response.setServiceId(booking.getService().getId());
            response.setServiceName(booking.getService().getServiceName());
        }
        if (booking.getMechanic() != null) {
            response.setMechanicId(booking.getMechanic().getId());
            response.setMechanicName(booking.getMechanic().getName());
        }
        response.setBookingDate(booking.getBookingDate());
        response.setBookingTime(booking.getBookingTime());
        response.setStatus(booking.getStatus());
        response.setTrackingLabel(trackingLabel(booking.getStatus()));
        response.setTotalPrice(booking.getTotalPrice());
        response.setNotes(booking.getNotes());
        response.setMechanicNotes(booking.getMechanicNotes());
        response.setChecklist(booking.getChecklist());
        response.setAssignedAt(booking.getAssignedAt());
        response.setStartedAt(booking.getStartedAt());
        response.setCompletedAt(booking.getCompletedAt());
        return response;
    }

    public static String trackingLabel(BookingStatus status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case PENDING -> "Booking Confirmed";
            case ASSIGNED -> "Mechanic Assigned";
            case IN_PROGRESS -> "Service In Progress";
            case COMPLETED -> "Completed";
            case CANCELLED -> "Cancelled";
        };
    }
}
