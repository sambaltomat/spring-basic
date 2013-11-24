package id.co.sambaltomat.model;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * User: HP
 * Date: 10/31/13
 * Time: 1:47 PM
 * To change this template use File | Settings | File Templates.
 */

@Entity
@Table(name="TestModelTabel")
public class TestModel {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;

    @Column(name = "HELLOMODEL")
    private String helloModel = "default model";

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHelloModel() {
        return helloModel;
    }

    public void setHelloModel(String helloModel) {
        this.helloModel = helloModel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TestModel)) return false;

        TestModel testModel = (TestModel) o;

        if (helloModel != null ? !helloModel.equals(testModel.helloModel) : testModel.helloModel != null) return false;
        if (id != null ? !id.equals(testModel.id) : testModel.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (helloModel != null ? helloModel.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TestModel{" +
                "id=" + id +
                ", helloModel='" + helloModel + '\'' +
                '}';
    }
}
