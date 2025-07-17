package greencity.dto.user;

import java.io.Serializable;
import greencity.constant.ValidationConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class PlaceAuthorDto implements Serializable {
    private Long id;
    private String name;

    @Email(message = ValidationConstants.INVALID_EMAIL)
    @NotBlank
    private String email;
}
