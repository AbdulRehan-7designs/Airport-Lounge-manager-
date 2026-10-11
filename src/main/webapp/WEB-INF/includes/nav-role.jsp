<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ page import="com.alm.model.User" %>
        <%@ page import="com.alm.config.Role" %>
            <% User navUser=(User) (session !=null ? session.getAttribute("user") : null); String role=navUser !=null &&
                navUser.getRole() !=null ? navUser.getRole().toUpperCase().trim() : "" ; if ("PASSANGER".equals(role))
                role="PASSENGER" ; // DB typo fix boolean isAdmin=Role.isAdmin(role); boolean
                canManager=Role.canAccessManager(role); boolean isPassenger=Role.isPassenger(role); %>
                <nav>
                    <a href="${pageContext.request.contextPath}/dashboard">Dashboard</a>
                    <% if (isPassenger) { %>
                        <a href="${pageContext.request.contextPath}/profile">My Profile</a>
                        <% } else { %>
                            <a href="${pageContext.request.contextPath}/passengers">Passengers</a>
                            <a href="${pageContext.request.contextPath}/access-requests">Access Requests</a>
                            <% if (canManager) { %><a href="${pageContext.request.contextPath}/lounges">Lounges</a>
                                <% } %>
                                    <% if (canManager) { %><a
                                            href="${pageContext.request.contextPath}/access-providers">Access
                                            Providers</a>
                                        <% } %>
                                            <% if (canManager) { %><a
                                                    href="${pageContext.request.contextPath}/reports">Reports</a>
                                                <% } %>
                                                    <% if (isAdmin) { %><a
                                                            href="${pageContext.request.contextPath}/users">Users</a>
                                                        <% } %>
                                                            <% } %>
                                                                <a href="${pageContext.request.contextPath}/logout"
                                                                    style="color: #ef4444;">Logout</a>
                </nav>