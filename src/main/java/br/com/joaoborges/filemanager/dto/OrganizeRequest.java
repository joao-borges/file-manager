package br.com.joaoborges.filemanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for file organization operation
 *
 * Contains validated parameters for organizing files by extension.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizeRequest {

    /**
     * Source directory containing files to organize
     */
    @NotBlank(message = "Source directory cannot be empty")
    @Pattern(
        regexp = "^[^<>:\"|?*\\x00-\\x1F]+$",
        message = "Source directory contains invalid characters"
    )
    private String sourceDirectory;

    /**
     * Destination directory for organized files
     */
    @NotBlank(message = "Destination directory cannot be empty")
    @Pattern(
        regexp = "^[^<>:\"|?*\\x00-\\x1F]+$",
        message = "Destination directory contains invalid characters"
    )
    private String destinationDirectory;
}
