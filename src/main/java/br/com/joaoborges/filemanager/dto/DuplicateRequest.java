package br.com.joaoborges.filemanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for duplicate finder operation
 *
 * Contains validated parameters for finding and removing duplicate files.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DuplicateRequest {

    /**
     * Directory to search for duplicate files
     */
    @NotBlank(message = "Directory cannot be empty")
    @Pattern(
        regexp = "^[^<>:\"|?*\\x00-\\x1F]+$",
        message = "Directory contains invalid characters"
    )
    private String directory;
}
