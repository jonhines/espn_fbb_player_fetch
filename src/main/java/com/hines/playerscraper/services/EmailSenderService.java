package com.hines.playerscraper.services;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailSenderService
{

    @Value("${sendgrid.key}")
    private String sendGridKey;

    @SneakyThrows
    public void sendMatchupEmail(Object jsonToSend, String email)
    {
        Email from = new Email("jonrhines@gmail.com");
        Email to = new Email(email);

        Mail mail = new Mail();
        mail.setTemplateId("d-7c127168cdea47078aae44177fa8a96e");
        mail.setFrom(from);

        Personalization personalization = new Personalization();
        personalization.addTo(to);
        personalization.addDynamicTemplateData("players", jsonToSend);
        personalization.setSubject("The League: Your daily matchups are here!");
        mail.addPersonalization(personalization);

        SendGrid sg = new SendGrid(sendGridKey);
        Request request = new Request();
        try
        {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            System.out.println(response.getStatusCode());
            System.out.println(response.getBody());
            System.out.println(response.getHeaders());
        } catch (IOException ex)
        {
            throw new RuntimeException("UH OH EMAIL FAIL!", ex);
        }
    }


    @SneakyThrows
    public void sendFABatsEmail(Object jsonToSend, String email)
    {
        Email from = new Email("jonrhines@gmail.com");
        Email to = new Email(email);

        Mail mail = new Mail();
        mail.setTemplateId("d-20d9d5f8e9bd4c1abc5ec1a103aa5475");
        mail.setFrom(from);

        Personalization personalization = new Personalization();
        personalization.addTo(to);
        personalization.addDynamicTemplateData("players", jsonToSend);
        personalization.setSubject("The League: Consider picking these bats up!");
        mail.addPersonalization(personalization);

        SendGrid sg = new SendGrid(sendGridKey);
        Request request = new Request();
        try
        {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            System.out.println(response.getStatusCode());
            System.out.println(response.getBody());
            System.out.println(response.getHeaders());
        } catch (IOException ex)
        {
            throw new RuntimeException("UH OH EMAIL FAIL!", ex);
        }
    }
}
