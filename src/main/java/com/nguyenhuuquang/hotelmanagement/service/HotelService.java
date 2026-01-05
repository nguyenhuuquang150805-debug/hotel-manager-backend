package com.nguyenhuuquang.hotelmanagement.service;

import java.util.List;

import com.nguyenhuuquang.hotelmanagement.dto.ServiceDTO;

public interface HotelService {
    ServiceDTO createService(ServiceDTO serviceDTO);

    ServiceDTO updateService(Long id, ServiceDTO serviceDTO);

    void deleteService(Long id);

    ServiceDTO getServiceById(Long id);

    List<ServiceDTO> getAllServices();

    List<ServiceDTO> getServicesByCategory(String category);

    List<ServiceDTO> getAvailableServices();
}