package org.smartregister.tasking.model;

import io.ona.kujaku.plugin.switcher.layer.BaseLayer;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class BaseLayerSwitchModel {

    public BaseLayer baseLayer;

    private boolean isDefault;
}
