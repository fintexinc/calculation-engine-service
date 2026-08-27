package ca.tangerine.pce.model.domain.enumeration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

import ca.tangerine.wm.commons.domain.allocation.EquitySectorAllocationType;
import ca.tangerine.wm.commons.domain.allocation.FixedIncomeSectorAllocationType;
import ca.tangerine.wm.commons.domain.allocation.SectorAllocationType;

/**
 * Guards the consolidated sector taxonomy the {@code sector-exposure} metric reports on. The enum itself lives in
 * catalogue-investment-commons, which has no test infrastructure of its own, and this service is its consumer of record
 * — so the totality of the two translations is pinned here rather than nowhere.
 *
 * <p>
 * The tests enumerate the source taxonomies rather than sampling them: a constant added upstream fails here instead of
 * silently becoming UNKNOWN in a client's donut.
 */
class SectorAllocationTypeTest {

  @ParameterizedTest
  @EnumSource(value = EquitySectorAllocationType.class, names = "UNKNOWN", mode = EnumSource.Mode.EXCLUDE)
  void shouldMapToSameNamedBucket_whenEquitySectorIsKnown(EquitySectorAllocationType equitySector) {
    SectorAllocationType bucket = SectorAllocationType.fromEquitySector(equitySector);

    assertThat(bucket).isNotEqualTo(SectorAllocationType.UNKNOWN);
    assertThat(bucket.name()).isEqualTo(equitySector.name());
    assertThat(bucket.getEquitySector()).isEqualTo(equitySector);
    assertThat(bucket.getEn()).isEqualTo(equitySector.getEn());
  }

  @ParameterizedTest
  @EnumSource(value = FixedIncomeSectorAllocationType.class, names = "UNKNOWN", mode = EnumSource.Mode.EXCLUDE)
  void shouldMapToSameNamedBucket_whenFixedIncomeSectorIsKnown(FixedIncomeSectorAllocationType fixedIncomeSector) {
    SectorAllocationType bucket = SectorAllocationType.fromFixedIncomeSector(fixedIncomeSector);

    assertThat(bucket).isNotEqualTo(SectorAllocationType.UNKNOWN);
    assertThat(bucket.name()).isEqualTo(fixedIncomeSector.name());
    assertThat(bucket.getFixedIncomeSector()).isEqualTo(fixedIncomeSector);
    assertThat(bucket.getEn()).isEqualTo(fixedIncomeSector.getEn());
  }

  @Test
  void shouldMapToUnknown_whenSourceSectorIsUnknownOrAbsent() {
    assertThat(SectorAllocationType.fromEquitySector(EquitySectorAllocationType.UNKNOWN))
        .isEqualTo(SectorAllocationType.UNKNOWN);
    assertThat(SectorAllocationType.fromFixedIncomeSector(FixedIncomeSectorAllocationType.UNKNOWN))
        .isEqualTo(SectorAllocationType.UNKNOWN);
    assertThat(SectorAllocationType.fromEquitySector(null)).isEqualTo(SectorAllocationType.UNKNOWN);
    assertThat(SectorAllocationType.fromFixedIncomeSector(null)).isEqualTo(SectorAllocationType.UNKNOWN);
  }

  /**
   * Equity and fixed income must not collide on a bucket: a bucket that both taxonomies fed would mix a share sector
   * with a bond sector under one label.
   */
  @Test
  void shouldKeepSleeveBucketsDisjoint_whenBothTaxonomiesAreMapped() {
    var equityBuckets = Arrays.stream(EquitySectorAllocationType.values())
        .filter(type -> type != EquitySectorAllocationType.UNKNOWN)
        .map(SectorAllocationType::fromEquitySector)
        .toList();
    var fixedIncomeBuckets = Arrays.stream(FixedIncomeSectorAllocationType.values())
        .filter(type -> type != FixedIncomeSectorAllocationType.UNKNOWN)
        .map(SectorAllocationType::fromFixedIncomeSector)
        .toList();

    assertThat(equityBuckets).doesNotHaveDuplicates().doesNotContainAnyElementsOf(fixedIncomeBuckets);
    assertThat(fixedIncomeBuckets).doesNotHaveDuplicates();
    assertThat(SectorAllocationType.values()).hasSize(equityBuckets.size() + fixedIncomeBuckets.size() + 2);
  }
}
