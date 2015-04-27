package de.fhg.fokus.ngni.osco.api.model;

/**
 * Created by lto on 20/04/15.
 */
public class TestClass {
    private String name;
    private String content;

    public TestClass() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestClass testClass = (TestClass) o;

        if (content != null ? !content.equals(testClass.content) : testClass.content != null) return false;
        if (name != null ? !name.equals(testClass.name) : testClass.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (content != null ? content.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TestClass{" +
                "name='" + name + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
