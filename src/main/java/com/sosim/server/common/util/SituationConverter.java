package com.sosim.server.common.util;

import com.sosim.server.event.Situation;
import org.springframework.core.convert.converter.Converter;

public class SituationConverter implements Converter<String, Situation> {
    @Override
    public Situation convert(String situation) {
        return Situation.getGround(situation);
    }
}
