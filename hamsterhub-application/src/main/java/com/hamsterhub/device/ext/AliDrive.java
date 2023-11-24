package com.hamsterhub.device.ext;

import com.hamsterhub.device.Storage;
import com.hamsterhub.service.dto.DeviceDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Component
public class AliDrive extends Storage {
    private Integer code = 1;
    private String name = "阿里云盘";
    private DeviceDTO device;

    @Override
    public AliDrive withDevice(DeviceDTO device) {
        return new AliDrive(this.code, this.name, device);
    }
}
