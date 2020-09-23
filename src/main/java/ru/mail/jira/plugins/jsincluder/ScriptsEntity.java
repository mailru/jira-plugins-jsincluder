package ru.mail.jira.plugins.jsincluder;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"unused", "MismatchedQueryAndUpdateOfCollection"})
@XmlRootElement
public class ScriptsEntity {
    @XmlElement
    private Set<ScriptDto> create = new HashSet<>();
    @XmlElement
    private Set<ScriptDto> view = new HashSet<>();
    @XmlElement
    private Set<ScriptDto> edit = new HashSet<>();
    @XmlElement
    private Set<ScriptDto> transition = new HashSet<>();
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
