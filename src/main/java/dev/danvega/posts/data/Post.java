package dev.danvega.posts.data;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;

public record Post(
        @Id
        Integer id,

        Integer userid,
        @NotEmpty
        String title,
        @NotEmpty
        String body,
        @Version Integer version
) {
}
