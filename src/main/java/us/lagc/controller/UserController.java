package us.lagc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/")
@RequestScope
public class UserController {

	@GetMapping("login")
	public ModelAndView getLoginForm() {
		ModelAndView mv = new ModelAndView();
		mv.setViewName("login");
		return mv;
	}

}
