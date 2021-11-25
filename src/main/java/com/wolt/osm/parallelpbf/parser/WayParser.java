/*
 * This file is part of parallelpbf.
 *
 *     parallelpbf is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Foobar is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Foobar.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.wolt.osm.parallelpbf.parser;

import com.wolt.osm.parallelpbf.entity.Way;
import crosby.binary.Osmformat;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

/**
 * Implements OSM Way parser.
 *
 */
@Slf4j
public final class WayParser extends BaseParser<Osmformat.Way, Consumer<Way>> {

    /**
     * Nano degrees scale.
     */
    private static final double NANO = .000000001;

    /**
     * Granularity, units of nanodegrees, used to store coordinates.
     */
    private final int granularity;

    /**
     * Offset value between the output coordinates coordinates and the granularity grid, in units of nanodegrees.
     * Latitude part.
     */
    private final long latOffset;

    /**
     * Offset value between the output coordinates coordinates and the granularity grid, in units of nanodegrees.
     * Longitude part.
     */
    private final long lonOffset;

    /**
     * Parent compatible constructor that sets callback and string table.
     * @param callback Callback to call on successful parse.
     * @param stringTable String table to use while parsing.
     * @param granularityValue Grid granularity value.
     * @param latOffsetValue Latitude offset of the grid.
     * @param lonOffsetValue Longitude offset of the grid.
     * @param
     *
     */
    public WayParser(final Consumer<Way> callback, final Osmformat.StringTable stringTable, final int granularityValue,
                     final long latOffsetValue, final long lonOffsetValue) {
        super(callback, stringTable);
        this.granularity = granularityValue;
        this.latOffset = latOffsetValue;
        this.lonOffset = lonOffsetValue;
    }

    @Override
    public void parse(final Osmformat.Way message) {
        long nodeId = 0;
        Way way = new Way(message.getId());
        way.setTags(parseTags(message.getKeysList(), message.getValsList()));
        way.setInfo(parseInfo(message));
        for (Long node : message.getRefsList()) {
            nodeId += node;
            way.getNodes().add(nodeId);
        }
        if (message.getLatList().size() == message.getRefsList().size()
            && message.getLatList().size() == message.getLonList().size()) {
            long currentLat = 0L;
            for (Long lat: message.getLatList()) {
                currentLat += lat;
                way.getLat().add(NANO * (latOffset + (granularity * currentLat)));
            }
            long currentLon = 0L;
            for (Long lon: message.getLonList()) {
                currentLon += lon;
                way.getLon().add(NANO * (lonOffset + (granularity * currentLon)));
            }
        }
        if (log.isDebugEnabled()) {
            log.debug(way.toString());
        }
        getCallback().accept(way);
    }
}
