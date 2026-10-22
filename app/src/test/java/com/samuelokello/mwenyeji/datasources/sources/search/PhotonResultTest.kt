package com.samuelokello.mwenyeji.datasources.sources.search

import com.samuelokello.mwenyeji.datasources.sources.search.dto.PhotonResponseDto
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PhotonResultTest {
    private val json = Json { ignoreUnknownKeys = true }

    // Trimmed from a real answer for "kencom", plus a nameless feature.
    private val response =
        """
        {"type":"FeatureCollection","features":[
          {"type":"Feature","geometry":{"type":"Point","coordinates":[36.8249899,-1.2856398]},
           "properties":{"osm_type":"N","osm_id":1537502919,"osm_key":"amenity","name":"Kencom",
             "street":"City Hall Way","locality":"Central Business District","city":"Nairobi","state":"Nairobi"}},
          {"type":"Feature","geometry":{"type":"Point","coordinates":[36.8,-1.3]},
           "properties":{"osm_type":"W","osm_id":5,"street":"Moi Avenue"}}
        ]}
        """.trimIndent()

    @Test
    fun `a named place becomes a result with its point and address`() {
        val results = json.decodeFromString<PhotonResponseDto>(response).features.mapNotNull { it.toSearchResultDto() }

        assertEquals(1, results.size)
        val kencom = results.single()
        assertEquals("N1537502919", kencom.id)
        assertEquals("Kencom", kencom.name)
        assertEquals("City Hall Way, Central Business District, Nairobi", kencom.fullAddress)
        assertEquals(-1.2856398, kencom.coordinates!!.latitude, 0.0)
        assertEquals(36.8249899, kencom.coordinates!!.longitude, 0.0)
    }

    @Test
    fun `a place without a name is dropped`() {
        val nameless = json.decodeFromString<PhotonResponseDto>(response).features[1]
        assertNull(nameless.toSearchResultDto())
    }
}
