package im.swyp.teumteumeat.domains.league.domain.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NameMaskerTest {

    @Test
    void 네_글자_이상이면_첫_글자와_마지막_글자를_제외하고_모두_마스킹한다() {
        assertThat(NameMasker.mask("김네글자")).isEqualTo("김**자");
        assertThat(NameMasker.mask("TestUser")).isEqualTo("T******r");
    }

    @Test
    void 세_글자는_가운데_글자를_마스킹한다() {
        assertThat(NameMasker.mask("김지민")).isEqualTo("김*민");
    }

    @Test
    void 두_글자는_마지막_글자를_마스킹한다() {
        assertThat(NameMasker.mask("김지")).isEqualTo("김*");
    }

    @Test
    void 한_글자는_그대로_노출한다() {
        assertThat(NameMasker.mask("김")).isEqualTo("김");
    }

    @Test
    void 이름이_없으면_익명으로_표시한다() {
        assertThat(NameMasker.mask(null)).isEqualTo("익명");
        assertThat(NameMasker.mask("  ")).isEqualTo("익명");
    }

    @Test
    void 이모지가_포함되어도_글자_단위로_마스킹한다() {
        assertThat(NameMasker.mask("🍪쿠키🍪")).isEqualTo("🍪**🍪");
    }
}
