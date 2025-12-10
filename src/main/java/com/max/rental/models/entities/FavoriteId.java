
package com.max.rental.models.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Составной ключ для таблицы favorites.
 * Связка renter_id + listing_id
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteId implements Serializable {

    private Long renterId;
    private Long listingId;
}