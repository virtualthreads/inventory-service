package com.aeropelican.inventoryservice.dto.response;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class APIResponse<T> {
    private T data;
    private String message;
    private Boolean success;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}