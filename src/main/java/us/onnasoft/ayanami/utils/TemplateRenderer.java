package us.onnasoft.ayanami.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

@Component
public class TemplateRenderer {

    @Autowired
    private SpringTemplateEngine templateEngine;

    /**
     * Renders a Thymeleaf template into a String.
     * 
     * @param templateName The name of the template (without the .html extension).
     * @param model A map containing variables to inject into the template.
     * @return The rendered HTML as a String.
     */
    public String renderTemplate(String templateName, Map<String, Object> model) {
        Context context = new Context();
        context.setVariables(model);
        return templateEngine.process(templateName, context);
    }
}
