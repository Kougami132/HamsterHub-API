package com.hamsterhub.service.device.ext;

import com.hamsterhub.service.device.Storage;
import com.hamsterhub.service.dto.DeviceDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Component
public class OneDrive extends Storage {
    private Integer code = 2;
    private String name = "OneDrive";
    private DeviceDTO device;

//    @Override
//    public OneDrive withDevice(DeviceDTO device) {
//        return new OneDrive(this.code, this.name, device);
//    }
}
