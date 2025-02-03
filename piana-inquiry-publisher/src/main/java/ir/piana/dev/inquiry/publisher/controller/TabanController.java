package ir.piana.dev.inquiry.publisher.controller;

import com.fasterxml.jackson.databind.JsonNode;
import ir.piana.boot.inquiry.common.dto.ResponseDto;
import ir.piana.boot.inquiry.thirdparty.insurance.vehicle.InsuranceVehicleThirdPartyService;
import ir.piana.boot.utils.errorprocessor.internal.InternalServerError;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/inquiry/insurance/vehicle/third-party")
public class TabanController {
    private final InsuranceVehicleThirdPartyService service;

    public TabanController(InsuranceVehicleThirdPartyService service) {
        this.service = service;
    }

    @GetMapping("search-by-vin")
    public ResponseEntity<ResponseDto> token() {
        try {
            JsonNode jsonNode = service.getByVin("NAS831100L5877148", "1399");
            return ResponseEntity.ok(new ResponseDto(jsonNode));
        } catch (Exception e) {
           throw InternalServerError.exception;
        }
    }
}
