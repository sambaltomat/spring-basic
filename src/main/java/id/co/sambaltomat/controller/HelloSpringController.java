package id.co.sambaltomat.controller;

import id.co.sambaltomat.core.service.GenericManager;
import id.co.sambaltomat.model.TestModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created with IntelliJ IDEA.
 * User: HP
 * Date: 10/28/13
 * Time: 2:39 PM
 * To change this template use File | Settings | File Templates.
 */
@Controller
public class HelloSpringController {

    @Autowired
    @Qualifier("testModelManager")
    public GenericManager<TestModel,Long> testModelManager;

    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public String printWelcome(ModelMap model) {

        TestModel tm = new TestModel();
        tm.setHelloModel("Hello -"+Math.random());
        testModelManager.save(tm);

        model.addAttribute("message", tm.toString());
        return "output";
    }

}