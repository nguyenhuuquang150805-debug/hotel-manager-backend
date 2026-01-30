package com.nguyenhuuquang.hotelmanagement.service.impl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nguyenhuuquang.hotelmanagement.dto.ServiceDTO;
import com.nguyenhuuquang.hotelmanagement.entity.Service;
import com.nguyenhuuquang.hotelmanagement.exception.ResourceNotFoundException;
import com.nguyenhuuquang.hotelmanagement.repository.ServiceRepository;
import com.nguyenhuuquang.hotelmanagement.service.SystemLogService;

@ExtendWith(MockitoExtension.class)
class HotelServiceImplTest {

    @Mock
    private ServiceRepository serviceRepo;

    @Mock
    private SystemLogService logService;

    @InjectMocks
    private HotelServiceImpl hotelService;

    private ServiceDTO serviceDTO;
    private Service service;

    @BeforeEach
    void setUp() {
        serviceDTO = ServiceDTO.builder()
                .name("Giặt ủi")
                .description("Dịch vụ giặt ủi")
                .price(BigDecimal.valueOf(50000))
                .category("Giặt ủi")
                .available(true)
                .build();

        service = Service.builder()
                .id(1L)
                .name("Giặt ủi")
                .description("Dịch vụ giặt ủi")
                .price(BigDecimal.valueOf(50000))
                .category("Giặt ủi")
                .available(true)
                .build();
    }

    @Test
    void testCreateService_Success() {
        when(serviceRepo.save(any(Service.class))).thenReturn(service);

        ServiceDTO result = hotelService.createService(serviceDTO);

        assertNotNull(result);
        assertEquals("Giặt ủi", result.getName());
        verify(serviceRepo).save(any(Service.class));
        verify(logService).log(any(), anyString(), anyString(), anyString());
    }

    @Test
    void testGetServiceById_Success() {
        when(serviceRepo.findById(1L)).thenReturn(Optional.of(service));

        ServiceDTO result = hotelService.getServiceById(1L);

        assertNotNull(result);
        assertEquals("Giặt ủi", result.getName());
        verify(serviceRepo).findById(1L);
    }

    @Test
    void testGetServiceById_NotFound() {
        when(serviceRepo.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> hotelService.getServiceById(999L));
    }

    @Test
    void testGetAllServices() {
        when(serviceRepo.findAll()).thenReturn(Arrays.asList(service));

        List<ServiceDTO> result = hotelService.getAllServices();

        assertEquals(1, result.size());
        verify(serviceRepo).findAll();
    }

    @Test
    void testGetServicesByCategory() {
        when(serviceRepo.findByCategory("Giặt ủi"))
                .thenReturn(Arrays.asList(service));

        List<ServiceDTO> result = hotelService.getServicesByCategory("Giặt ủi");

        assertEquals(1, result.size());
        verify(serviceRepo).findByCategory("Giặt ủi");
    }

    @Test
    void testGetAvailableServices() {
        when(serviceRepo.findByAvailable(true))
                .thenReturn(Arrays.asList(service));

        List<ServiceDTO> result = hotelService.getAvailableServices();

        assertEquals(1, result.size());
        assertTrue(result.get(0).getAvailable());
    }

    @Test
    void testUpdateService_Success() {
        when(serviceRepo.findById(1L)).thenReturn(Optional.of(service));
        when(serviceRepo.save(any(Service.class))).thenReturn(service);

        serviceDTO.setPrice(BigDecimal.valueOf(60000));
        ServiceDTO result = hotelService.updateService(1L, serviceDTO);

        assertNotNull(result);
        verify(serviceRepo).save(any(Service.class));
    }

    @Test
    void testUpdateService_NotFound() {
        when(serviceRepo.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> hotelService.updateService(999L, serviceDTO));
    }

    @Test
    void testDeleteService_Success() {
        when(serviceRepo.findById(1L)).thenReturn(Optional.of(service));

        hotelService.deleteService(1L);

        verify(serviceRepo).delete(service);
        verify(logService).log(any(), anyString(), anyString(), anyString());
    }

    @Test
    void testDeleteService_NotFound() {
        when(serviceRepo.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> hotelService.deleteService(999L));
    }
}