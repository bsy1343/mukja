// OptionDef.java — 옵션 정의. type: single|toggle|counter
package com.mukja.menu.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OptionDef(String label, String type, boolean required,
                        List<OptionChoice> choices, Integer extra, Integer max) {}
