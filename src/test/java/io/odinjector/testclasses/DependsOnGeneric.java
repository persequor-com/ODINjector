package io.odinjector.testclasses;

import javax.inject.Inject;
import java.util.List;

public class DependsOnGeneric {
    private MyGeneric<TestInterface1> list;

    @Inject
    public DependsOnGeneric(MyGeneric<TestInterface1> list) {
        this.list = list;
    }


}
