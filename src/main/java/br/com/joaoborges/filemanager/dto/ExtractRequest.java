package br.com.joaoborges.filemanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for file extraction operation
 *
 * Contains validated parameters for extracting files from subdirectories.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtractRequest {

    /**
     * Source directory to extract files from
     */
    @NotBlank(message = "Source directory cannot be empty")
    @Pattern(
        regexp = "^[^<>:\"|?*\\x00-\\x1F]+$",
        message = "Source directory contains invalid characters"
    )
    private String sourceDirectory;

    /**
     * Destination directory for extracted files
     */
    @NotBlank(message = "Destination directory cannot be empty")
    @Pattern(
        regexp = "^[^<>:\"|?*\\x00-\\x1F]+$",
        message = "Destination directory contains invalid characters"
    )
    private String destinationDirectory;
}
