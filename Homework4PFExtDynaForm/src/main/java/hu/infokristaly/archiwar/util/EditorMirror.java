package hu.infokristaly.archiwar.util;

import hu.infokristaly.archiwar.front.annotations.EditοrFieldInfo;

import javax.faces.component.UIComponent;

import hu.infokristaly.archiwar.front.annotations.EditorInfo;

public class EditorMirror {

    @EditorInfo(fields={@EditοrFieldInfo()})
    public UIComponent parent;
    
}
