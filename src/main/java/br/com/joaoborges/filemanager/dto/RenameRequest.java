package br.com.joaoborges.filemanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for file rename operation
 *
 * Contains validated parameters for renaming files in a directory.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RenameRequest {

    /**
     * Source directory path containing files to rename
     */
    @NotBlank(message = "Source directory cannot be empty")
    @Pattern(
        regexp = "^[^<>:\"|?*\\x00-\\x1F]+$",
        message = "Source directory contains invalid characters"
    )
    private String sourceDirectory;

    /**
     * Whether to include files in subdirectories
     */
    private boolean includeSubDirectories;
}
