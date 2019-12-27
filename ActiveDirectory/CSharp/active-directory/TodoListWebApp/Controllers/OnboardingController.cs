﻿using System;
using System.Collections.Generic;
using System.Data;
using System.Data.Entity;
using System.Linq;
using System.Net;
using System.Web;
using System.Web.Mvc;
using TodoListWebApp.Models;
using TodoListWebApp.DAL;
using System.Configuration;
using Microsoft.IdentityModel.Clients.ActiveDirectory;
using System.Threading.Tasks;

namespace TodoListWebApp.Controllers
{
    // controller that handles the onboarding of new tenants and new individual users
    // operates by starting an OAuth2 request on behalf of the user
    // during that request, the user is asked whether he/she consent for the app to gain access to the specified directory permissions.    
    public class OnboardingController : Controller
    {
        private TodoListWebAppContext db = new TodoListWebAppContext();

        // GET: /Onboarding/SignUp
        public ActionResult SignUp()
        {
            return View();
        }

        // POST: /Onboarding/SignUp
        [HttpPost]
        [ValidateAntiForgeryToken]
        public ActionResult SignUp([Bind(Include="ID,Name,AdminConsented")] Tenant tenant)
        {
            // generate a random value to identify the request
            string stateMarker = Guid.NewGuid().ToString();
            // store it in the temporary entry for the tenant, we'll use it later to assess if the request was originated from us
            // this is necessary if we want to prevent attackers from provisioning themselves to access our app without having gone through our onboarding process (e.g. payments, etc)
            tenant.IssValue = stateMarker;
            tenant.Created = DateTime.Now;
            db.Tenants.Add(tenant);
            db.SaveChanges();

            //create an OAuth2 request, using the web app as the client.
            //this will trigger a consent flow that will provision the app in the target tenant
            string authorizationRequest = String.Format(
                "https://login.chinacloudapi.cn/common/oauth2/authorize?response_type=code&client_id={0}&resource={1}&redirect_uri={2}&state={3}",
                 Uri.EscapeDataString(ConfigurationManager.AppSettings["ida:ClientID"]),
                 Uri.EscapeDataString("https://graph.chinacloudapi.cn"),
                 Uri.EscapeDataString(this.Request.Url.GetLeftPart(UriPartial.Authority).ToString() + "/Onboarding/ProcessCode"),
                 Uri.EscapeDataString(stateMarker)
                 );
            //if the prospect customer wants to provision the app for all users in his/her tenant, the request must be modified accordingly
            if (tenant.AdminConsented)
                authorizationRequest += String.Format("&prompt={0}", Uri.EscapeDataString("admin_consent"));
            // send the user to consent
            return new RedirectResult(authorizationRequest);
        }

         // GET: /TOnboarding/ProcessCode
        public async Task<ActionResult> ProcessCode(string code, string error, string error_description, string resource, string state)
        {
            // Is this a response to a request we generated? Let's see if the state is carrying an ID we previously saved
            // ---if we don't, return an error            
            if (db.Tenants.FirstOrDefault(a => a.IssValue == state) == null)
            {
                // TODO: prettify
                return View("Error");
            }
            else
            {
                // ---if the response is indeed from a request we generated
                // ------get a token for the Graph, that will provide us with information abut the caller
                ClientCredential credential = new ClientCredential(ConfigurationManager.AppSettings["ida:ClientID"],
                                                                   ConfigurationManager.AppSettings["ida:Password"]);
                AuthenticationContext authContext = new AuthenticationContext("https://login.chinacloudapi.cn/common/");
                AuthenticationResult result = await authContext.AcquireTokenByAuthorizationCodeAsync(
                    code, new Uri(Request.Url.GetLeftPart(UriPartial.Path)), credential);

                var myTenant = db.Tenants.FirstOrDefault(a => a.IssValue == state);
                // if this was an admin consent, save the tenant
                if (myTenant.AdminConsented)
                {
                    // ------read the tenantID out of the Graph token and use it to create the issuer string
                    string issuer = String.Format("https://sts.microsoft.com/{0}/", result.TenantId);
                    myTenant.IssValue = issuer;
                }
                else
                //otherwise, remove the temporary entry and save just the user
                {
                    if (db.Users.FirstOrDefault(a => (a.UPN == result.UserInfo.DisplayableId) && (a.TenantID == result.TenantId)) == null)
                    {
                        db.Users.Add(new User { UPN = result.UserInfo.DisplayableId, TenantID = result.TenantId });
                    }
                    db.Tenants.Remove(myTenant);
                }

                // remove older, unclaimed entries
                DateTime tenMinsAgo = DateTime.Now.Subtract(new TimeSpan(0, 10, 0)); // workaround for Linq to entities
                var garbage = db.Tenants.Where(a => (!a.IssValue.StartsWith("https") && (a.Created < tenMinsAgo)));
                foreach (Tenant t in garbage)
                    db.Tenants.Remove(t);

                db.SaveChanges();
                // ------return a view claiming success, inviting the user to sign in
                return View();
            }
        }
        protected override void Dispose(bool disposing)
        {
            if (disposing)
            {
                db.Dispose();
            }
            base.Dispose(disposing);
        }
    }
}
