package org.ansj.test;

import static org.elasticsearch.common.settings.ImmutableSettings.Builder.EMPTY_SETTINGS;

import java.util.Map.Entry;

import org.elasticsearch.common.collect.ImmutableMap;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.env.Environment;
import org.elasticsearch.node.internal.InternalSettingsPreparer;

public class ElasticSearchTest {
    public static void main(String[] args) {
        Tuple<Settings, Environment> prepareSettings = InternalSettingsPreparer.prepareSettings(Builder.EMPTY_SETTINGS, true);
        Settings v1 = prepareSettings.v1() ;
        ImmutableMap<String, String> asMap = v1.getAsMap() ;
        for (Entry<String, String> entry : asMap.entrySet()) {
            System.out.println(entry.getKey()+"\t"+entry.getValue());
        }
    }
}
