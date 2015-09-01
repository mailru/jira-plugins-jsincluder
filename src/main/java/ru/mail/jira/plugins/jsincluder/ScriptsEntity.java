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
    private List<String> create = new ArrayList<String>();
    @XmlElement
    private List<String> view = new ArrayList<String>();
    @XmlElement
    private List<String> edit = new ArrayList<String>();
    @XmlElement
    private List<String> transition = new ArrayList<String>();
    @XmlElement
    private Map<String, Object> params = new HashMap<String, Object>();

    public void addCreateScript(String code) {
        create.add(code);
    }

    public void addViewScript(String code) {
        view.add(code);
    }

    public void addEditScript(String code) {
        edit.add(code);
    }

    public void addTransitionScript(String code) {
        transition.add(code);
    }

    public void putParam(String key, Object value) {
        params.put(key, value);
    }
}
