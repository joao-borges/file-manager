package br.com.joaoborges.filemanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for photo organization operation
 *
 * Contains validated parameters for organizing photos by EXIF date.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoOrganizeRequest {

    /**
     * Source directory containing photos/videos
     */
    @NotBlank(message = "Source directory cannot be empty")
    @Pattern(
        regexp = "^[^<>:\"|?*\\x00-\\x1F]+$",
        message = "Source directory contains invalid characters"
    )
    private String sourceDirectory;

    /**
     * Destination directory for organized photos
     */
    @NotBlank(message = "Destination directory cannot be empty")
    @Pattern(
        regexp = "^[^<>:\"|?*\\x00-\\x1F]+$",
        message = "Destination directory contains invalid characters"
    )
    private String destinationDirectory;
}
