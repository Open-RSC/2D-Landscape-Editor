package org.openrsc.editor.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openrsc.editor.model.template.TerrainTemplate;

@AllArgsConstructor
@Getter
public class TerrainPresetSelectedEvent {
    TerrainTemplate template;
}
