package cn.et;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;


@Controller
public class SendController {
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired  
    private LoadBalancerClient loadBalancer;
	
	/**  
     * 启动多个发布者 端口不一致 程序名相同   
     * 使用  
     * @LoadBalanced必须添加  
     * @return  
     */  
	@ResponseBody
    @RequestMapping("/choosePub")  
    public String choosePub() {  
        StringBuffer sb=new StringBuffer();  
        for(int i=0;i<=10;i++) {  
            ServiceInstance ss=loadBalancer.choose("EMAILSERVER");//从两个idserver中选择一个 这里涉及到选择算法  
            sb.append(ss.getUri().toString()+"<br/>");
        }  
        return sb.toString();  
    }    
	
	@GetMapping("/sendClient")
	public String send(String email_to,String email_subject ,String email_content){
		//调用email服务
		String controller="/send";
		//通过注册中心客户端负载均衡  获取一台主机来调用
		try {
			controller += "?email_to="+email_to+"&email_subject="+email_subject+"&email_content="+email_content;
			String result=restTemplate.getForObject("http://EMAILSERVER"+controller, String.class);
			
		} catch (RestClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "redirect:/error.html";
		}
		return "redirect:/suc.html";
	}
	/**
	 * 演示post
	 * @param email_to
	 * @param email_subject
	 * @param email_content
	 * @return
	 */
	@PostMapping("/sendClientpost")
	public String postsend(String email_to,String email_subject ,String email_content){
		
		try {
			//
			HttpHeaders headers=new HttpHeaders();
			Map<String,Object> map=new HashMap<String,Object>();
			map.put("email_to",email_to );
			map.put("email_subject", email_subject);
			map.put("email_content", email_content);
			HttpEntity<Map> request=new HttpEntity<Map>(map, headers);
			String result=restTemplate.postForObject("http://EMAILSERVER/send",request, String.class);
			
		} catch (RestClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "redirect:/error.html";
		}
		return "redirect:/suc.html";
	}
	/**
	 * 演示调用sendmail的/user/这个请求
	 * @return
	 */
	@ResponseBody
	@GetMapping("/invokeUser")
	public String invokeUser(String id){
		
		String resul=restTemplate.getForObject("http://EMAILSERVER/user/{id}",String.class,id);
		return resul;
	}
}
