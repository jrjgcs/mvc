package com.lzh.bank.web.servlet;

import com.lzh.bank.web.exceptions.AppException;
import com.lzh.bank.web.exceptions.MoneyNotEnoughException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

/**
 * 分析存在的问题:
 *  缺点1> 代码的复用性太差(代码的重用性太差)
 *  导致缺点1的原因是?
 *      因为没有进行"职能分工",没有独立的组件的概念,所以没有代码的复用性,代码和代码之间的耦合度太高,扩展力太差
 *  缺点2> 耦合度高,导致了代码很难进行扩展。
 *  缺点3> 操作数据库的代码和业务逻辑混杂在一起很容易出错。编写代码的时候很容易出错，无法专注于业务逻辑的编写。
 *
 * 分析以下Servlet负责了什么?
 *  1> 负责了数据接受
 *  2> 负责了核心的业务处理
 *  3> 负责了数据库表中数据的CRUD操作(Create[增] Retrieve[查] Update[改] Delete[删])
 *  4> 负责了页面的数据展示
 *  ....
 *
 *  公司中一般有很多员工，每个员工都各司其职，为什么这样？
 *      保洁阿姨负责打扫卫生
 *      杜老师负责教学大纲的制定
 *      郭老师负责上课
 *  我们公司只有一个员工。这个员工负责所有的事情。生病了。-->公司倒闭了。
 *
 *
 *
 */
@WebServlet("/transfer")
public class BankServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {


        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String fromActno = request.getParameter("fromActno");
        String toActno = request.getParameter("toActno");
        double money = Double.parseDouble(request.getParameter("money"));
        // 首先判断转账的用户余额是否够
        Connection conn = null;
        PreparedStatement ps = null;
        PreparedStatement ps2 = null;
        PreparedStatement ps3= null;
        ResultSet rs = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://localhost:3306/mvc";
            String user = "root";
            String password = "000000";
            conn = DriverManager.getConnection(url,user,password);
            String sql = "select balance from t_act where actno = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1,fromActno);
            rs = ps.executeQuery();
            if(rs.next()){
                // 查看余额是否够
                if(rs.getDouble("balance") < money){
                    // 余额不足的情况下(抛出异常)
                    throw new MoneyNotEnoughException("余额不足");
                }
                // 执行到这里说明余额够了
                // 开始转账
                // act001账户减去10000
                // act002账户加上10000
                conn.setAutoCommit(false);
                String sql2 = "update t_act set balance = balance - ? where actno = ?";
                ps2 = conn.prepareStatement(sql2);
                ps2.setDouble(1,money);
                ps2.setString(2,fromActno);
                int count = 0;
                count = ps2.executeUpdate();


                String sql3 = "update t_act set balance = balance + ? where actno = ?";
                ps3 = conn.prepareStatement(sql3);
                ps3.setDouble(1,money);
                ps3.setString(2,toActno);
                count += ps3.executeUpdate();

                if (count==2) {
                    out.print("转账成功");
                    conn.commit();
                }else {
                    if(conn != null) {
                        conn.rollback();
                    }
                    throw new AppException("App出现异常,请联系客服");
                }


            }
        } catch (Exception e) {
            out.print(e.getMessage());
        } finally {
            if(rs != null){
                try {
                    rs.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            if(ps != null){
                try {
                    ps.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            if(ps2 != null){
                try {
                    ps.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            if(ps3 != null){
                try {
                    ps.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            if(conn != null){
                try {
                    conn.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}























