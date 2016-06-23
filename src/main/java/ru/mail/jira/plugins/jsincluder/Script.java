package ru.mail.jira.plugins.jsincluder;

import net.java.ao.Entity;
import net.java.ao.OneToMany;
import net.java.ao.schema.StringLength;

@SuppressWarnings("unused")
public interface Script extends Entity {
    String getName();
    void setName(String name);

    @StringLength(StringLength.UNLIMITED)
    String getCode();
    void setCode(String code);

    @OneToMany
    public Binding[] getBindings();
}