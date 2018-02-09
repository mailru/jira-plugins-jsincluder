package ru.mail.jira.plugins.jsincluder;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unused", "MismatchedQueryAndUpdateOfCollection"})
@XmlRootElement
public class ScriptsEntity {
    @XmlElement
    private List<ScriptDto> create = new ArrayList<ScriptDto>();
    @XmlElement
    private List<ScriptDto> view = new ArrayList<ScriptDto>();
    @XmlElement
    private List<ScriptDto> edit = new ArrayList<ScriptDto>();
    @XmlElement
    private List<ScriptDto> transition = new ArrayList<ScriptDto>();
    @XmlElement
    private Map<String, Object> params = new HashMap<String, Object>();

    public void addCreateScript(ScriptDto script) {
        create.add(script);
    }

    public void addViewScript(ScriptDto script) {
        view.add(script);
    }

    public void addEditScript(ScriptDto script) {
        edit.add(script);
    }

    public void addTransitionScript(ScriptDto script) {
        transition.add(script);
    }

    public void putParam(String key, Object value) {
        params.put(key, value);
    }
}
