package probes;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import probes.subPackage.TestDto;

import java.util.List;

@Getter
@EqualsAndHashCode(callSuper = true)
public class TestDtoChild extends TestDto {
    String prop1;
    List<String> prop2;

    public TestDtoChild(String booga, String booga2, ProbeRetVal retVal, String prop1, List<String> prop2) {
        super(booga, booga2, retVal);
        this.prop1 = prop1;
        this.prop2 = prop2;
    }
}
