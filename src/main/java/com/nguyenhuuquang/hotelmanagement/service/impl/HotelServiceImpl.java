package com.nguyenhuuquang.hotelmanagement.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nguyenhuuquang.hotelmanagement.dto.ServiceDTO;
import com.nguyenhuuquang.hotelmanagement.entity.enums.LogType;
import com.nguyenhuuquang.hotelmanagement.exception.ResourceNotFoundException;
import com.nguyenhuuquang.hotelmanagement.repository.ServiceRepository;
import com.nguyenhuuquang.hotelmanagement.service.HotelService;
import com.nguyenhuuquang.hotelmanagement.service.SystemLogService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {

    private final ServiceRepository serviceRepo;
    private final SystemLogService logService;

    @Override
    @Transactional
    public ServiceDTO createService(ServiceDTO serviceDTO) {
        com.nguyenhuuquang.hotelmanagement.entity.Service service = com.nguyenhuuquang.hotelmanagement.entity.Service
                .builder()
                .name(serviceDTO.getName())
                .description(serviceDTO.getDescription())
                .price(serviceDTO.getPrice())
                .category(serviceDTO.getCategory())
                .available(true)
                .build();

        service = serviceRepo.save(service);

        logService.log(LogType.SUCCESS, "Thêm dịch vụ", "Admin",
                String.format("Đã thêm dịch vụ %s", service.getName()));

        return convertToDTO(service);
    }

    @Override
    @Transactional
    public ServiceDTO updateService(Long id, ServiceDTO serviceDTO) {
        com.nguyenhuuquang.hotelmanagement.entity.Service service = serviceRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dịch vụ"));

        service.setName(serviceDTO.getName());
        service.setDescription(serviceDTO.getDescription());
        service.setPrice(serviceDTO.getPrice());
        service.setCategory(serviceDTO.getCategory());
        service.setAvailable(serviceDTO.getAvailable());

        service = serviceRepo.save(service);

        logService.log(LogType.INFO, "Cập nhật dịch vụ", "Admin",
                String.format("Đã cập nhật dịch vụ %s", service.getName()));

        return convertToDTO(service);
    }

    @Override
    @Transactional
    public void deleteService(Long id) {
        com.nguyenhuuquang.hotelmanagement.entity.Service service = serviceRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dịch vụ"));

        String name = service.getName();
        serviceRepo.delete(service);

        logService.log(LogType.WARNING, "Xóa dịch vụ", "Admin",
                String.format("Đã xóa dịch vụ %s", name));
    }

    @Override
    public ServiceDTO getServiceById(Long id) {
        com.nguyenhuuquang.hotelmanagement.entity.Service service = serviceRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dịch vụ"));
        return convertToDTO(service);
    }

    @Override
    public List<ServiceDTO> getAllServices() {
        return serviceRepo.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ServiceDTO> getServicesByCategory(String category) {
        return serviceRepo.findByCategory(category)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ServiceDTO> getAvailableServices() {
        return serviceRepo.findByAvailable(true)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ServiceDTO convertToDTO(com.nguyenhuuquang.hotelmanagement.entity.Service service) {
        return ServiceDTO.builder()
                .id(service.getId())
                .name(service.getName())
                .description(service.getDescription())
                .price(service.getPrice())
                .category(service.getCategory())
                .available(service.getAvailable())
                .build();
    }
}