# -*- coding: utf-8 -*-
import os

OUTPUT_DIR = 'docs/figures'

BASE_STYLE = "strokeColor=#000000;fillColor=#ffffff;fontColor=#000000;strokeWidth=1;"
LINE_STYLE = "endArrow=none;html=1;strokeColor=#000000;strokeWidth=1;"

def create_figure_3_1():
    return '''<mxfile host="app.diagrams.net" agent="Mozilla/5.0" version="21.0.0">
  <diagram id="diagram1" name="Page-1">
    <mxGraphModel dx="800" dy="600" grid="1" gridSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="1" pageScale="1" pageWidth="827" pageHeight="1169" math="0" shadow="0" background="#ffffff">
      <root>
        <mxCell id="0"/>
        <mxCell id="1" parent="0"/>
        <mxCell id="2" value="Minecraft反作弊系统" style="text;html=1;strokeColor=none;fillColor=#ffffff;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=16;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="250" y="20" width="200" height="30" as="geometry"/>
        </mxCell>
        <mxCell id="3" value="玩家管理模块" style="rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;strokeWidth=1;fontSize=12;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="40" y="80" width="120" height="40" as="geometry"/>
        </mxCell>
        <mxCell id="4" value="作弊检测模块" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontSize=12;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="180" y="80" width="120" height="40" as="geometry"/>
        </mxCell>
        <mxCell id="5" value="封禁管理模块" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontSize=12;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="320" y="80" width="120" height="40" as="geometry"/>
        </mxCell>
        <mxCell id="6" value="举报管理模块" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontSize=12;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="460" y="80" width="120" height="40" as="geometry"/>
        </mxCell>
        <mxCell id="7" value="白名单管理模块" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontSize=12;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="600" y="80" width="120" height="40" as="geometry"/>
        </mxCell>
        <mxCell id="8" value="玩家信息查询" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontSize=10;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="20" y="150" width="80" height="30" as="geometry"/>
        </mxCell>
        <mxCell id="9" value="玩家状态管理" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontSize=10;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="110" y="150" width="80" height="30" as="geometry"/>
        </mxCell>
        <mxCell id="10" value="飞行检测" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontSize=10;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="160" y="150" width="70" height="30" as="geometry"/>
        </mxCell>
        <mxCell id="11" value="加速检测" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontSize=10;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="240" y="150" width="70" height="30" as="geometry"/>
        </mxCell>
        <mxCell id="12" value="封禁操作" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontSize=10;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="300" y="150" width="70" height="30" as="geometry"/>
        </mxCell>
        <mxCell id="13" value="解封操作" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontSize=10;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="380" y="150" width="70" height="30" as="geometry"/>
        </mxCell>
        <mxCell id="14" value="举报提交" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontSize=10;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="440" y="150" width="70" height="30" as="geometry"/>
        </mxCell>
        <mxCell id="15" value="举报处理" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontSize=10;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="520" y="150" width="70" height="30" as="geometry"/>
        </mxCell>
        <mxCell id="16" value="白名单添加" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontSize=10;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="580" y="150" width="70" height="30" as="geometry"/>
        </mxCell>
        <mxCell id="17" value="白名单移除" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontSize=10;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="660" y="150" width="70" height="30" as="geometry"/>
        </mxCell>
        <mxCell id="18" style="endArrow=none;html=1;strokeColor=#000000;" edge="1" parent="1" source="2" target="3">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="19" style="endArrow=none;html=1;strokeColor=#000000;" edge="1" parent="1" source="2" target="4">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="20" style="endArrow=none;html=1;strokeColor=#000000;" edge="1" parent="1" source="2" target="5">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="21" style="endArrow=none;html=1;strokeColor=#000000;" edge="1" parent="1" source="2" target="6">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="22" style="endArrow=none;html=1;strokeColor=#000000;" edge="1" parent="1" source="2" target="7">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="23" style="endArrow=none;html=1;strokeColor=#000000;" edge="1" parent="1" source="3" target="8">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="24" style="endArrow=none;html=1;strokeColor=#000000;" edge="1" parent="1" source="3" target="9">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="25" style="endArrow=none;html=1;strokeColor=#000000;" edge="1" parent="1" source="4" target="10">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="26" style="endArrow=none;html=1;strokeColor=#000000;" edge="1" parent="1" source="4" target="11">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="27" style="endArrow=none;html=1;strokeColor=#000000;" edge="1" parent="1" source="5" target="12">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="28" style="endArrow=none;html=1;strokeColor=#000000;" edge="1" parent="1" source="5" target="13">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="29" style="endArrow=none;html=1;strokeColor=#000000;" edge="1" parent="1" source="6" target="14">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="30" style="endArrow=none;html=1;strokeColor=#000000;" edge="1" parent="1" source="6" target="15">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="31" style="endArrow=none;html=1;strokeColor=#000000;" edge="1" parent="1" source="7" target="16">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="32" style="endArrow=none;html=1;strokeColor=#000000;" edge="1" parent="1" source="7" target="17">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
      </root>
    </mxGraphModel>
  </diagram>
</mxfile>'''

def create_figure_3_2():
    return '''<mxfile host="app.diagrams.net" agent="Mozilla/5.0" version="21.0.0">
  <diagram id="diagram1" name="Page-1">
    <mxGraphModel dx="800" dy="600" grid="1" gridSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="1" pageScale="1" pageWidth="827" pageHeight="1169" math="0" shadow="0" background="#ffffff">
      <root>
        <mxCell id="0"/>
        <mxCell id="1" parent="0"/>
        <mxCell id="2" value="管理员" style="shape=umlActor;verticalLabelPosition=bottom;verticalAlign=top;html=1;outlineConnect=0;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="50" y="200" width="40" height="80" as="geometry"/>
        </mxCell>
        <mxCell id="3" value="玩家" style="shape=umlActor;verticalLabelPosition=bottom;verticalAlign=top;html=1;outlineConnect=0;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="50" y="400" width="40" height="80" as="geometry"/>
        </mxCell>
        <mxCell id="4" value="Minecraft反作弊系统" style="rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontSize=14;fontStyle=1;verticalAlign=top;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="150" y="40" width="500" height="500" as="geometry"/>
        </mxCell>
        <mxCell id="5" value="登录系统" style="ellipse;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="300" y="80" width="100" height="40" as="geometry"/>
        </mxCell>
        <mxCell id="6" value="玩家管理" style="ellipse;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="300" y="140" width="100" height="40" as="geometry"/>
        </mxCell>
        <mxCell id="7" value="作弊检测" style="ellipse;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="300" y="200" width="100" height="40" as="geometry"/>
        </mxCell>
        <mxCell id="8" value="封禁管理" style="ellipse;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="300" y="260" width="100" height="40" as="geometry"/>
        </mxCell>
        <mxCell id="9" value="举报管理" style="ellipse;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="300" y="320" width="100" height="40" as="geometry"/>
        </mxCell>
        <mxCell id="10" value="白名单管理" style="ellipse;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="300" y="380" width="100" height="40" as="geometry"/>
        </mxCell>
        <mxCell id="11" value="数据统计" style="ellipse;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="300" y="440" width="100" height="40" as="geometry"/>
        </mxCell>
        <mxCell id="12" value="提交举报" style="ellipse;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="500" y="320" width="100" height="40" as="geometry"/>
        </mxCell>
        <mxCell id="13" style="endArrow=open;html=1;endSize=8;strokeColor=#000000;" edge="1" parent="1" source="2" target="5">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="14" style="endArrow=open;html=1;endSize=8;strokeColor=#000000;" edge="1" parent="1" source="2" target="6">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="15" style="endArrow=open;html=1;endSize=8;strokeColor=#000000;" edge="1" parent="1" source="2" target="7">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="16" style="endArrow=open;html=1;endSize=8;strokeColor=#000000;" edge="1" parent="1" source="2" target="8">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="17" style="endArrow=open;html=1;endSize=8;strokeColor=#000000;" edge="1" parent="1" source="2" target="9">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="18" style="endArrow=open;html=1;endSize=8;strokeColor=#000000;" edge="1" parent="1" source="2" target="10">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="19" style="endArrow=open;html=1;endSize=8;strokeColor=#000000;" edge="1" parent="1" source="2" target="11">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="20" style="endArrow=open;html=1;endSize=8;strokeColor=#000000;" edge="1" parent="1" source="3" target="12">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
      </root>
    </mxGraphModel>
  </diagram>
</mxfile>'''

def create_figure_3_3():
    return '''<mxfile host="app.diagrams.net" agent="Mozilla/5.0" version="21.0.0">
  <diagram id="diagram1" name="Page-1">
    <mxGraphModel dx="800" dy="600" grid="1" gridSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="1" pageScale="1" pageWidth="827" pageHeight="1169" math="0" shadow="0" background="#ffffff">
      <root>
        <mxCell id="0"/>
        <mxCell id="1" parent="0"/>
        <mxCell id="2" value="外部实体&#xa;(玩家/管理员)" style="rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="40" y="200" width="100" height="60" as="geometry"/>
        </mxCell>
        <mxCell id="3" value="Minecraft反作弊系统" style="rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="350" y="180" width="150" height="100" as="geometry"/>
        </mxCell>
        <mxCell id="4" value="操作请求" style="endArrow=classic;html=1;strokeColor=#000000;fontColor=#000000;" edge="1" parent="1" source="2" target="3">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="5" value="处理结果" style="endArrow=classic;html=1;strokeColor=#000000;fontColor=#000000;" edge="1" parent="1" source="3" target="2">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
      </root>
    </mxGraphModel>
  </diagram>
</mxfile>'''

def create_figure_3_4():
    return '''<mxfile host="app.diagrams.net" agent="Mozilla/5.0" version="21.0.0">
  <diagram id="diagram1" name="Page-1">
    <mxGraphModel dx="800" dy="600" grid="1" gridSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="1" pageScale="1" pageWidth="827" pageHeight="1169" math="0" shadow="0" background="#ffffff">
      <root>
        <mxCell id="0"/>
        <mxCell id="1" parent="0"/>
        <mxCell id="2" value="1.0&#xa;玩家管理" style="ellipse;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="150" y="80" width="100" height="60" as="geometry"/>
        </mxCell>
        <mxCell id="3" value="2.0&#xa;作弊检测" style="ellipse;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="350" y="80" width="100" height="60" as="geometry"/>
        </mxCell>
        <mxCell id="4" value="3.0&#xa;封禁管理" style="ellipse;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="550" y="80" width="100" height="60" as="geometry"/>
        </mxCell>
        <mxCell id="5" value="4.0&#xa;举报处理" style="ellipse;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="250" y="200" width="100" height="60" as="geometry"/>
        </mxCell>
        <mxCell id="6" value="5.0&#xa;白名单管理" style="ellipse;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="450" y="200" width="100" height="60" as="geometry"/>
        </mxCell>
        <mxCell id="7" value="玩家信息" style="shape=parallelogram;perimeter=parallelogramPerimeter;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="50" y="200" width="100" height="40" as="geometry"/>
        </mxCell>
        <mxCell id="8" value="作弊记录" style="shape=parallelogram;perimeter=parallelogramPerimeter;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="350" y="180" width="100" height="40" as="geometry"/>
        </mxCell>
        <mxCell id="9" value="封禁记录" style="shape=parallelogram;perimeter=parallelogramPerimeter;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="650" y="200" width="100" height="40" as="geometry"/>
        </mxCell>
        <mxCell id="10" value="玩家数据" style="endArrow=classic;html=1;strokeColor=#000000;fontColor=#000000;" edge="1" parent="1" source="2" target="3">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="11" value="检测结果" style="endArrow=classic;html=1;strokeColor=#000000;fontColor=#000000;" edge="1" parent="1" source="3" target="4">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="12" value="封禁信息" style="endArrow=classic;html=1;strokeColor=#000000;fontColor=#000000;" edge="1" parent="1" source="4" target="5">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="13" value="举报信息" style="endArrow=classic;html=1;strokeColor=#000000;fontColor=#000000;" edge="1" parent="1" source="5" target="6">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
      </root>
    </mxGraphModel>
  </diagram>
</mxfile>'''

def create_figure_4_1():
    return '''<mxfile host="app.diagrams.net" agent="Mozilla/5.0" version="21.0.0">
  <diagram id="diagram1" name="Page-1">
    <mxGraphModel dx="800" dy="600" grid="1" gridSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="1" pageScale="1" pageWidth="827" pageHeight="1169" math="0" shadow="0" background="#ffffff">
      <root>
        <mxCell id="0"/>
        <mxCell id="1" parent="0"/>
        <mxCell id="2" value="系统架构图" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=16;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="300" y="10" width="200" height="30" as="geometry"/>
        </mxCell>
        <mxCell id="3" value="用户层" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="50" y="50" width="700" height="60" as="geometry"/>
        </mxCell>
        <mxCell id="4" value="Web浏览器" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="100" y="60" width="100" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="5" value="Minecraft客户端" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="250" y="60" width="120" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="6" value="移动端" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="420" y="60" width="80" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="7" value="表现层" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="50" y="130" width="700" height="60" as="geometry"/>
        </mxCell>
        <mxCell id="8" value="Vue 3前端" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="100" y="140" width="100" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="9" value="RESTful API" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="250" y="140" width="100" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="10" value="WebSocket" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="400" y="140" width="100" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="11" value="业务层" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="50" y="210" width="700" height="80" as="geometry"/>
        </mxCell>
        <mxCell id="12" value="Spring Boot 3.2" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="100" y="220" width="120" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="13" value="玩家服务" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="250" y="220" width="80" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="14" value="检测服务" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="350" y="220" width="80" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="15" value="封禁服务" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="450" y="220" width="80" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="16" value="举报服务" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="550" y="220" width="80" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="17" value="数据层" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="50" y="310" width="700" height="60" as="geometry"/>
        </mxCell>
        <mxCell id="18" value="MySQL数据库" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="150" y="320" width="120" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="19" value="Redis缓存" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="350" y="320" width="100" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="20" value="文件存储" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="520" y="320" width="100" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="21" value="Minecraft服务器层" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="50" y="390" width="700" height="60" as="geometry"/>
        </mxCell>
        <mxCell id="22" value="Spigot/Paper服务器" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="150" y="400" width="140" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="23" value="反作弊插件" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="380" y="400" width="100" height="35" as="geometry"/>
        </mxCell>
      </root>
    </mxGraphModel>
  </diagram>
</mxfile>'''

def create_figure_4_2():
    return '''<mxfile host="app.diagrams.net" agent="Mozilla/5.0" version="21.0.0">
  <diagram id="diagram1" name="Page-1">
    <mxGraphModel dx="800" dy="600" grid="1" gridSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="1" pageScale="1" pageWidth="827" pageHeight="1169" math="0" shadow="0" background="#ffffff">
      <root>
        <mxCell id="0"/>
        <mxCell id="1" parent="0"/>
        <mxCell id="2" value="开始" style="ellipse;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="350" y="30" width="60" height="40" as="geometry"/>
        </mxCell>
        <mxCell id="3" value="用户登录" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="320" y="100" width="120" height="40" as="geometry"/>
        </mxCell>
        <mxCell id="4" value="身份验证" style="diamond;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="330" y="170" width="100" height="50" as="geometry"/>
        </mxCell>
        <mxCell id="5" value="验证成功?" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=10;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="355" y="175" width="50" height="20" as="geometry"/>
        </mxCell>
        <mxCell id="6" value="进入系统" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="320" y="260" width="120" height="40" as="geometry"/>
        </mxCell>
        <mxCell id="7" value="执行操作" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="320" y="340" width="120" height="40" as="geometry"/>
        </mxCell>
        <mxCell id="8" value="处理请求" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="320" y="420" width="120" height="40" as="geometry"/>
        </mxCell>
        <mxCell id="9" value="返回结果" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="320" y="500" width="120" height="40" as="geometry"/>
        </mxCell>
        <mxCell id="10" value="结束" style="ellipse;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="350" y="580" width="60" height="40" as="geometry"/>
        </mxCell>
        <mxCell id="11" value="显示错误" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="500" y="180" width="100" height="30" as="geometry"/>
        </mxCell>
        <mxCell id="12" style="endArrow=classic;html=1;strokeColor=#000000;" edge="1" parent="1" source="2" target="3">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="13" style="endArrow=classic;html=1;strokeColor=#000000;" edge="1" parent="1" source="3" target="4">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="14" value="是" style="endArrow=classic;html=1;strokeColor=#000000;fontColor=#000000;" edge="1" parent="1" source="4" target="6">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="15" value="否" style="endArrow=classic;html=1;strokeColor=#000000;fontColor=#000000;" edge="1" parent="1" source="4" target="11">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="16" style="endArrow=classic;html=1;strokeColor=#000000;" edge="1" parent="1" source="11" target="3">
          <mxGeometry relative="1" as="geometry">
            <Array as="points">
              <mxPoint x="550" y="120"/>
            </Array>
          </mxGeometry>
        </mxCell>
        <mxCell id="17" style="endArrow=classic;html=1;strokeColor=#000000;" edge="1" parent="1" source="6" target="7">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="18" style="endArrow=classic;html=1;strokeColor=#000000;" edge="1" parent="1" source="7" target="8">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="19" style="endArrow=classic;html=1;strokeColor=#000000;" edge="1" parent="1" source="8" target="9">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="20" style="endArrow=classic;html=1;strokeColor=#000000;" edge="1" parent="1" source="9" target="10">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
      </root>
    </mxGraphModel>
  </diagram>
</mxfile>'''

def create_figure_4_3():
    return '''<mxfile host="app.diagrams.net" agent="Mozilla/5.0" version="21.0.0">
  <diagram id="diagram1" name="Page-1">
    <mxGraphModel dx="800" dy="600" grid="1" gridSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="1" pageScale="1" pageWidth="827" pageHeight="1169" math="0" shadow="0" background="#ffffff">
      <root>
        <mxCell id="0"/>
        <mxCell id="1" parent="0"/>
        <mxCell id="2" value="开始" style="ellipse;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="350" y="20" width="60" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="3" value="收集玩家数据&#xa;(位置、速度、动作)" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="300" y="80" width="160" height="45" as="geometry"/>
        </mxCell>
        <mxCell id="4" value="数据分析" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="320" y="150" width="120" height="40" as="geometry"/>
        </mxCell>
        <mxCell id="5" value="飞行检测" style="diamond;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="150" y="220" width="100" height="50" as="geometry"/>
        </mxCell>
        <mxCell id="6" value="加速检测" style="diamond;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="340" y="220" width="100" height="50" as="geometry"/>
        </mxCell>
        <mxCell id="7" value="其他检测" style="diamond;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="530" y="220" width="100" height="50" as="geometry"/>
        </mxCell>
        <mxCell id="8" value="记录作弊" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="100" y="310" width="80" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="8b" value="记录作弊" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="300" y="310" width="80" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="8c" value="记录作弊" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="540" y="310" width="80" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="9" value="正常" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="340" y="380" width="80" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="10" value="累计违规&#xa;达到阈值?" style="diamond;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="320" y="450" width="120" height="60" as="geometry"/>
        </mxCell>
        <mxCell id="11" value="自动封禁" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="200" y="540" width="100" height="40" as="geometry"/>
        </mxCell>
        <mxCell id="12" value="通知管理员" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="400" y="540" width="100" height="40" as="geometry"/>
        </mxCell>
        <mxCell id="13" value="结束" style="ellipse;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="350" y="620" width="60" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="14" style="endArrow=classic;html=1;strokeColor=#000000;" edge="1" parent="1" source="2" target="3">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="15" style="endArrow=classic;html=1;strokeColor=#000000;" edge="1" parent="1" source="3" target="4">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="16" style="endArrow=classic;html=1;strokeColor=#000000;" edge="1" parent="1" source="4" target="5">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="17" style="endArrow=classic;html=1;strokeColor=#000000;" edge="1" parent="1" source="4" target="6">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="18" style="endArrow=classic;html=1;strokeColor=#000000;" edge="1" parent="1" source="4" target="7">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="19" value="异常" style="endArrow=classic;html=1;strokeColor=#000000;fontColor=#000000;" edge="1" parent="1" source="5" target="8">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="20" value="异常" style="endArrow=classic;html=1;strokeColor=#000000;fontColor=#000000;" edge="1" parent="1" source="6" target="8b">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="21" value="异常" style="endArrow=classic;html=1;strokeColor=#000000;fontColor=#000000;" edge="1" parent="1" source="7" target="8c">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="22" style="endArrow=classic;html=1;strokeColor=#000000;" edge="1" parent="1" source="8" target="9">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="23" style="endArrow=classic;html=1;strokeColor=#000000;" edge="1" parent="1" source="9" target="10">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="24" value="是" style="endArrow=classic;html=1;strokeColor=#000000;fontColor=#000000;" edge="1" parent="1" source="10" target="11">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="25" value="否" style="endArrow=classic;html=1;strokeColor=#000000;fontColor=#000000;" edge="1" parent="1" source="10" target="12">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="26" style="endArrow=classic;html=1;strokeColor=#000000;" edge="1" parent="1" source="11" target="13">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="27" style="endArrow=classic;html=1;strokeColor=#000000;" edge="1" parent="1" source="12" target="13">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
      </root>
    </mxGraphModel>
  </diagram>
</mxfile>'''

def create_figure_4_4():
    return '''<mxfile host="app.diagrams.net" agent="Mozilla/5.0" version="21.0.0">
  <diagram id="diagram1" name="Page-1">
    <mxGraphModel dx="800" dy="600" grid="1" gridSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="1" pageScale="1" pageWidth="827" pageHeight="1169" math="0" shadow="0" background="#ffffff">
      <root>
        <mxCell id="0"/>
        <mxCell id="1" parent="0"/>
        <mxCell id="2" value="开始" style="ellipse;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="350" y="20" width="60" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="3" value="玩家提交举报" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="310" y="80" width="140" height="40" as="geometry"/>
        </mxCell>
        <mxCell id="4" value="验证举报内容" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="310" y="150" width="140" height="40" as="geometry"/>
        </mxCell>
        <mxCell id="5" value="保存举报记录" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="310" y="220" width="140" height="40" as="geometry"/>
        </mxCell>
        <mxCell id="6" value="管理员审核" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="310" y="290" width="140" height="40" as="geometry"/>
        </mxCell>
        <mxCell id="7" value="举报有效?" style="diamond;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="330" y="360" width="100" height="50" as="geometry"/>
        </mxCell>
        <mxCell id="8" value="处理违规玩家" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="200" y="440" width="120" height="40" as="geometry"/>
        </mxCell>
        <mxCell id="9" value="驳回举报" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="450" y="440" width="100" height="40" as="geometry"/>
        </mxCell>
        <mxCell id="10" value="通知相关玩家" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="310" y="520" width="140" height="40" as="geometry"/>
        </mxCell>
        <mxCell id="11" value="结束" style="ellipse;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="350" y="600" width="60" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="12" style="endArrow=classic;html=1;strokeColor=#000000;" edge="1" parent="1" source="2" target="3">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="13" style="endArrow=classic;html=1;strokeColor=#000000;" edge="1" parent="1" source="3" target="4">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="14" style="endArrow=classic;html=1;strokeColor=#000000;" edge="1" parent="1" source="4" target="5">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="15" style="endArrow=classic;html=1;strokeColor=#000000;" edge="1" parent="1" source="5" target="6">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="16" style="endArrow=classic;html=1;strokeColor=#000000;" edge="1" parent="1" source="6" target="7">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="17" value="是" style="endArrow=classic;html=1;strokeColor=#000000;fontColor=#000000;" edge="1" parent="1" source="7" target="8">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="18" value="否" style="endArrow=classic;html=1;strokeColor=#000000;fontColor=#000000;" edge="1" parent="1" source="7" target="9">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="19" style="endArrow=classic;html=1;strokeColor=#000000;" edge="1" parent="1" source="8" target="10">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="20" style="endArrow=classic;html=1;strokeColor=#000000;" edge="1" parent="1" source="9" target="10">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="21" style="endArrow=classic;html=1;strokeColor=#000000;" edge="1" parent="1" source="10" target="11">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
      </root>
    </mxGraphModel>
  </diagram>
</mxfile>'''

def create_figure_4_5():
    return '''<mxfile host="app.diagrams.net" agent="Mozilla/5.0" version="21.0.0">
  <diagram id="diagram1" name="Page-1">
    <mxGraphModel dx="800" dy="600" grid="1" gridSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="1" pageScale="1" pageWidth="827" pageHeight="1169" math="0" shadow="0" background="#ffffff">
      <root>
        <mxCell id="0"/>
        <mxCell id="1" parent="0"/>
        <mxCell id="2" value="系统总体E-R图" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=14;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="300" y="10" width="200" height="30" as="geometry"/>
        </mxCell>
        <mxCell id="3" value="玩家" style="rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="320" y="80" width="80" height="40" as="geometry"/>
        </mxCell>
        <mxCell id="4" value="管理员" style="rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="520" y="80" width="80" height="40" as="geometry"/>
        </mxCell>
        <mxCell id="5" value="作弊记录" style="rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="150" y="200" width="100" height="40" as="geometry"/>
        </mxCell>
        <mxCell id="6" value="封禁记录" style="rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="320" y="200" width="100" height="40" as="geometry"/>
        </mxCell>
        <mxCell id="7" value="举报记录" style="rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="500" y="200" width="100" height="40" as="geometry"/>
        </mxCell>
        <mxCell id="8" value="白名单" style="rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="320" y="320" width="100" height="40" as="geometry"/>
        </mxCell>
        <mxCell id="9" value="1:n" style="endArrow=none;html=1;strokeColor=#000000;fontColor=#000000;" edge="1" parent="1" source="3" target="5">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="10" value="1:n" style="endArrow=none;html=1;strokeColor=#000000;fontColor=#000000;" edge="1" parent="1" source="3" target="6">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="11" value="1:n" style="endArrow=none;html=1;strokeColor=#000000;fontColor=#000000;" edge="1" parent="1" source="3" target="7">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="12" value="1:n" style="endArrow=none;html=1;strokeColor=#000000;fontColor=#000000;" edge="1" parent="1" source="3" target="8">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="13" value="1:n" style="endArrow=none;html=1;strokeColor=#000000;fontColor=#000000;" edge="1" parent="1" source="4" target="6">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        <mxCell id="14" value="1:n" style="endArrow=none;html=1;strokeColor=#000000;fontColor=#000000;" edge="1" parent="1" source="4" target="7">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
      </root>
    </mxGraphModel>
  </diagram>
</mxfile>'''

def create_er_figure(entity_name, attributes):
    attrs_xml = ""
    for i, attr in enumerate(attributes):
        attrs_xml += f'''        <mxCell id="{i+3}" value="{attr}" style="ellipse;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="{180 + (i % 3) * 120}" y="{120 + (i // 3) * 80}" width="80" height="40" as="geometry"/>
        </mxCell>
'''
    
    return f'''<mxfile host="app.diagrams.net" agent="Mozilla/5.0" version="21.0.0">
  <diagram id="diagram1" name="Page-1">
    <mxGraphModel dx="800" dy="600" grid="1" gridSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="1" pageScale="1" pageWidth="827" pageHeight="1169" math="0" shadow="0" background="#ffffff">
      <root>
        <mxCell id="0"/>
        <mxCell id="1" parent="0"/>
        <mxCell id="2" value="{entity_name}" style="rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontStyle=1;fontSize=14;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="280" y="40" width="140" height="40" as="geometry"/>
        </mxCell>
{attrs_xml}      </root>
    </mxGraphModel>
  </diagram>
</mxfile>'''

def create_figure_4_6():
    return create_er_figure("玩家", ["玩家ID", "用户名", "UUID", "IP地址", "状态", "注册时间"])

def create_figure_4_7():
    return create_er_figure("作弊记录", ["记录ID", "玩家ID", "作弊类型", "检测时间", "详情", "服务器"])

def create_figure_4_8():
    return create_er_figure("封禁记录", ["封禁ID", "玩家ID", "操作员ID", "封禁原因", "开始时间", "结束时间", "封禁类型"])

def create_figure_4_9():
    return create_er_figure("举报记录", ["举报ID", "举报人ID", "被举报人ID", "举报原因", "举报时间", "处理状态", "处理人ID"])

def create_figure_4_10():
    return create_er_figure("管理员", ["管理员ID", "用户名", "密码", "角色", "创建时间", "最后登录"])

def create_figure_4_11():
    return create_er_figure("白名单", ["记录ID", "玩家ID", "添加人ID", "添加原因", "添加时间", "备注"])

def create_figure_5_1():
    return '''<mxfile host="app.diagrams.net" agent="Mozilla/5.0" version="21.0.0">
  <diagram id="diagram1" name="Page-1">
    <mxGraphModel dx="800" dy="600" grid="1" gridSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="1" pageScale="1" pageWidth="827" pageHeight="1169" math="0" shadow="0" background="#ffffff">
      <root>
        <mxCell id="0"/>
        <mxCell id="1" parent="0"/>
        <mxCell id="2" value="" style="rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="100" y="50" width="600" height="400" as="geometry"/>
        </mxCell>
        <mxCell id="3" value="Minecraft反作弊系统 - 登录" style="rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontStyle=1;fontSize=14;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="100" y="50" width="600" height="40" as="geometry"/>
        </mxCell>
        <mxCell id="4" value="系统登录" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=18;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="300" y="200" width="200" height="30" as="geometry"/>
        </mxCell>
        <mxCell id="5" value="用户名:" style="text;html=1;strokeColor=none;fillColor=none;align=right;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=12;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="200" y="260" width="80" height="30" as="geometry"/>
        </mxCell>
        <mxCell id="6" value="请输入用户名" style="rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontSize=11;align=left;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="290" y="260" width="200" height="30" as="geometry"/>
        </mxCell>
        <mxCell id="7" value="密码:" style="text;html=1;strokeColor=none;fillColor=none;align=right;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=12;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="200" y="310" width="80" height="30" as="geometry"/>
        </mxCell>
        <mxCell id="8" value="请输入密码" style="rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontSize=11;align=left;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="290" y="310" width="200" height="30" as="geometry"/>
        </mxCell>
        <mxCell id="9" value="登 录" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontSize=14;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="290" y="370" width="200" height="40" as="geometry"/>
        </mxCell>
        <mxCell id="10" value="© 2026 Minecraft反作弊系统" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=10;fontColor=#666666;" vertex="1" parent="1">
          <mxGeometry x="250" y="430" width="200" height="20" as="geometry"/>
        </mxCell>
      </root>
    </mxGraphModel>
  </diagram>
</mxfile>'''

def create_figure_5_2():
    return '''<mxfile host="app.diagrams.net" agent="Mozilla/5.0" version="21.0.0">
  <diagram id="diagram1" name="Page-1">
    <mxGraphModel dx="800" dy="600" grid="1" gridSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="1" pageScale="1" pageWidth="827" pageHeight="1169" math="0" shadow="0" background="#ffffff">
      <root>
        <mxCell id="0"/>
        <mxCell id="1" parent="0"/>
        <mxCell id="2" value="" style="rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="50" y="30" width="700" height="450" as="geometry"/>
        </mxCell>
        <mxCell id="3" value="LoginController.java - 用户登录核心代码" style="text;html=1;strokeColor=none;fillColor=none;align=left;verticalAlign=top;whiteSpace=wrap;rounded=0;fontSize=11;fontColor=#000000;fontFamily=Courier New" vertex="1" parent="1">
          <mxGeometry x="60" y="40" width="680" height="20" as="geometry"/>
        </mxCell>
        <mxCell id="4" value="@RestController&#xa;@RequestMapping(&quot;/api/auth&quot;)&#xa;public class LoginController {&#xa;    &#xa;    @Autowired&#xa;    private AuthService authService;&#xa;    &#xa;    @PostMapping(&quot;/login&quot;)&#xa;    public ResponseEntity&lt;?&gt; login(@RequestBody LoginRequest request) {&#xa;        try {&#xa;            String token = authService.authenticate(&#xa;                request.getUsername(), &#xa;                request.getPassword()&#xa;            );&#xa;            return ResponseEntity.ok(new LoginResponse(token));&#xa;        } catch (AuthException e) {&#xa;            return ResponseEntity.status(401)&#xa;                .body(new ErrorResponse(e.getMessage()));&#xa;        }&#xa;    }&#xa;}" style="text;html=1;strokeColor=none;fillColor=none;align=left;verticalAlign=top;whiteSpace=wrap;rounded=0;fontSize=10;fontColor=#000000;fontFamily=Courier New;spacingLeft=10;spacingTop=5" vertex="1" parent="1">
          <mxGeometry x="60" y="70" width="680" height="400" as="geometry"/>
        </mxCell>
      </root>
    </mxGraphModel>
  </diagram>
</mxfile>'''

def create_figure_5_3():
    return '''<mxfile host="app.diagrams.net" agent="Mozilla/5.0" version="21.0.0">
  <diagram id="diagram1" name="Page-1">
    <mxGraphModel dx="800" dy="600" grid="1" gridSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="1" pageScale="1" pageWidth="827" pageHeight="1169" math="0" shadow="0" background="#ffffff">
      <root>
        <mxCell id="0"/>
        <mxCell id="1" parent="0"/>
        <mxCell id="2" value="" style="rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="50" y="30" width="700" height="450" as="geometry"/>
        </mxCell>
        <mxCell id="3" value="Minecraft反作弊系统 - 玩家管理" style="rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontStyle=1;fontSize=14;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="50" y="30" width="700" height="40" as="geometry"/>
        </mxCell>
        <mxCell id="4" value="" style="rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="50" y="70" width="120" height="410" as="geometry"/>
        </mxCell>
        <mxCell id="5" value="仪表盘" style="text;html=1;strokeColor=none;fillColor=none;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=11;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="60" y="80" width="100" height="30" as="geometry"/>
        </mxCell>
        <mxCell id="6" value="玩家管理" style="text;html=1;strokeColor=none;fillColor=none;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=11;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="60" y="115" width="100" height="30" as="geometry"/>
        </mxCell>
        <mxCell id="7" value="封禁管理" style="text;html=1;strokeColor=none;fillColor=none;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=11;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="60" y="150" width="100" height="30" as="geometry"/>
        </mxCell>
        <mxCell id="8" value="举报管理" style="text;html=1;strokeColor=none;fillColor=none;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=11;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="60" y="185" width="100" height="30" as="geometry"/>
        </mxCell>
        <mxCell id="9" value="白名单" style="text;html=1;strokeColor=none;fillColor=none;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=11;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="60" y="220" width="100" height="30" as="geometry"/>
        </mxCell>
        <mxCell id="10" value="搜索玩家..." style="rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontSize=11;align=left;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="180" y="80" width="200" height="30" as="geometry"/>
        </mxCell>
        <mxCell id="11" value="搜索" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontSize=11;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="390" y="80" width="60" height="30" as="geometry"/>
        </mxCell>
        <mxCell id="12" value="" style="rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="180" y="130" width="550" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="13" value="玩家名" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=11;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="180" y="130" width="100" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="14" value="UUID" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=11;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="280" y="130" width="150" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="15" value="状态" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=11;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="430" y="130" width="80" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="16" value="操作" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=11;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="510" y="130" width="220" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="17" value="" style="rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="180" y="165" width="550" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="18" value="Player_001" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=10;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="180" y="165" width="100" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="19" value="a1b2c3d4-e5f6..." style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=10;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="280" y="165" width="150" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="20" value="在线" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=10;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="430" y="165" width="80" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="21" value="查看 | 封禁 | 白名单" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=10;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="510" y="165" width="220" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="22" value="" style="rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="180" y="200" width="550" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="23" value="Player_002" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=10;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="180" y="200" width="100" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="24" value="f6e5d4c3-b2a1..." style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=10;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="280" y="200" width="150" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="25" value="离线" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=10;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="430" y="200" width="80" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="26" value="查看 | 封禁 | 白名单" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=10;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="510" y="200" width="220" height="35" as="geometry"/>
        </mxCell>
      </root>
    </mxGraphModel>
  </diagram>
</mxfile>'''

def create_figure_5_4():
    return '''<mxfile host="app.diagrams.net" agent="Mozilla/5.0" version="21.0.0">
  <diagram id="diagram1" name="Page-1">
    <mxGraphModel dx="800" dy="600" grid="1" gridSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="1" pageScale="1" pageWidth="827" pageHeight="1169" math="0" shadow="0" background="#ffffff">
      <root>
        <mxCell id="0"/>
        <mxCell id="1" parent="0"/>
        <mxCell id="2" value="" style="rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="50" y="30" width="700" height="450" as="geometry"/>
        </mxCell>
        <mxCell id="3" value="Minecraft反作弊系统 - 仪表盘" style="rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontStyle=1;fontSize=14;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="50" y="30" width="700" height="40" as="geometry"/>
        </mxCell>
        <mxCell id="4" value="" style="rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="50" y="70" width="120" height="410" as="geometry"/>
        </mxCell>
        <mxCell id="5" value="仪表盘" style="text;html=1;strokeColor=none;fillColor=none;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=11;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="60" y="80" width="100" height="30" as="geometry"/>
        </mxCell>
        <mxCell id="6" value="玩家管理" style="text;html=1;strokeColor=none;fillColor=none;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=11;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="60" y="115" width="100" height="30" as="geometry"/>
        </mxCell>
        <mxCell id="7" value="封禁管理" style="text;html=1;strokeColor=none;fillColor=none;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=11;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="60" y="150" width="100" height="30" as="geometry"/>
        </mxCell>
        <mxCell id="8" value="举报管理" style="text;html=1;strokeColor=none;fillColor=none;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=11;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="60" y="185" width="100" height="30" as="geometry"/>
        </mxCell>
        <mxCell id="9" value="白名单" style="text;html=1;strokeColor=none;fillColor=none;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=11;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="60" y="220" width="100" height="30" as="geometry"/>
        </mxCell>
        <mxCell id="10" value="在线玩家&#xa;1,234" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontSize=12;fontStyle=1;verticalAlign=middle;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="180" y="80" width="130" height="60" as="geometry"/>
        </mxCell>
        <mxCell id="11" value="今日封禁&#xa;56" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontSize=12;fontStyle=1;verticalAlign=middle;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="330" y="80" width="130" height="60" as="geometry"/>
        </mxCell>
        <mxCell id="12" value="待处理举报&#xa;23" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontSize=12;fontStyle=1;verticalAlign=middle;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="480" y="80" width="130" height="60" as="geometry"/>
        </mxCell>
        <mxCell id="13" value="白名单玩家&#xa;89" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontSize=12;fontStyle=1;verticalAlign=middle;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="630" y="80" width="110" height="60" as="geometry"/>
        </mxCell>
        <mxCell id="14" value="作弊检测趋势图" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontSize=11;verticalAlign=top;align=left;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="180" y="160" width="280" height="150" as="geometry"/>
        </mxCell>
        <mxCell id="15" value="玩家活跃度" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontSize=11;verticalAlign=top;align=left;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="480" y="160" width="260" height="150" as="geometry"/>
        </mxCell>
        <mxCell id="16" value="最近活动" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontSize=10;verticalAlign=top;align=left;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="180" y="330" width="560" height="140" as="geometry"/>
        </mxCell>
      </root>
    </mxGraphModel>
  </diagram>
</mxfile>'''

def create_figure_5_5():
    return '''<mxfile host="app.diagrams.net" agent="Mozilla/5.0" version="21.0.0">
  <diagram id="diagram1" name="Page-1">
    <mxGraphModel dx="800" dy="600" grid="1" gridSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="1" pageScale="1" pageWidth="827" pageHeight="1169" math="0" shadow="0" background="#ffffff">
      <root>
        <mxCell id="0"/>
        <mxCell id="1" parent="0"/>
        <mxCell id="2" value="" style="rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="50" y="30" width="700" height="450" as="geometry"/>
        </mxCell>
        <mxCell id="3" value="WebSocketConfig.java - WebSocket配置代码" style="text;html=1;strokeColor=none;fillColor=none;align=left;verticalAlign=top;whiteSpace=wrap;rounded=0;fontSize=11;fontColor=#000000;fontFamily=Courier New" vertex="1" parent="1">
          <mxGeometry x="60" y="40" width="680" height="20" as="geometry"/>
        </mxCell>
        <mxCell id="4" value="@Configuration&#xa;@EnableWebSocket&#xa;public class WebSocketConfig implements WebSocketConfigurer {&#xa;    &#xa;    @Autowired&#xa;    private AntiCheatWebSocketHandler webSocketHandler;&#xa;    &#xa;    @Override&#xa;    public void registerWebSocketHandlers(&#xa;            WebSocketHandlerRegistry registry) {&#xa;        registry.addHandler(webSocketHandler, &quot;/ws/anticheat&quot;)&#xa;            .setAllowedOrigins(&quot;*&quot;)&#xa;            .addInterceptors(new HttpSessionHandshakeInterceptor());&#xa;    }&#xa;}&#xa;&#xa;@Component&#xa;public class AntiCheatWebSocketHandler extends TextWebSocketHandler {&#xa;    &#xa;    @Override&#xa;    protected void handleTextMessage(WebSocketSession session, &#xa;            TextMessage message) throws Exception {&#xa;        // 处理实时作弊检测消息&#xa;        JSONObject data = JSON.parseObject(message.getPayload());&#xa;        broadcastToAll(data.toString());&#xa;    }&#xa;}" style="text;html=1;strokeColor=none;fillColor=none;align=left;verticalAlign=top;whiteSpace=wrap;rounded=0;fontSize=10;fontColor=#000000;fontFamily=Courier New;spacingLeft=10;spacingTop=5" vertex="1" parent="1">
          <mxGeometry x="60" y="70" width="680" height="400" as="geometry"/>
        </mxCell>
      </root>
    </mxGraphModel>
  </diagram>
</mxfile>'''

def create_figure_5_6():
    return '''<mxfile host="app.diagrams.net" agent="Mozilla/5.0" version="21.0.0">
  <diagram id="diagram1" name="Page-1">
    <mxGraphModel dx="800" dy="600" grid="1" gridSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="1" pageScale="1" pageWidth="827" pageHeight="1169" math="0" shadow="0" background="#ffffff">
      <root>
        <mxCell id="0"/>
        <mxCell id="1" parent="0"/>
        <mxCell id="2" value="" style="rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="50" y="30" width="700" height="450" as="geometry"/>
        </mxCell>
        <mxCell id="3" value="Minecraft反作弊系统 - 封禁管理" style="rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontStyle=1;fontSize=14;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="50" y="30" width="700" height="40" as="geometry"/>
        </mxCell>
        <mxCell id="4" value="" style="rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="50" y="70" width="120" height="410" as="geometry"/>
        </mxCell>
        <mxCell id="5" value="仪表盘" style="text;html=1;strokeColor=none;fillColor=none;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=11;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="60" y="80" width="100" height="30" as="geometry"/>
        </mxCell>
        <mxCell id="6" value="玩家管理" style="text;html=1;strokeColor=none;fillColor=none;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=11;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="60" y="115" width="100" height="30" as="geometry"/>
        </mxCell>
        <mxCell id="7" value="封禁管理" style="text;html=1;strokeColor=none;fillColor=none;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=11;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="60" y="150" width="100" height="30" as="geometry"/>
        </mxCell>
        <mxCell id="8" value="举报管理" style="text;html=1;strokeColor=none;fillColor=none;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=11;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="60" y="185" width="100" height="30" as="geometry"/>
        </mxCell>
        <mxCell id="9" value="白名单" style="text;html=1;strokeColor=none;fillColor=none;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=11;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="60" y="220" width="100" height="30" as="geometry"/>
        </mxCell>
        <mxCell id="10" value="+ 添加封禁" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontSize=11;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="180" y="80" width="100" height="30" as="geometry"/>
        </mxCell>
        <mxCell id="11" value="" style="rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="180" y="130" width="550" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="12" value="玩家" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=11;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="180" y="130" width="80" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="13" value="封禁原因" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=11;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="260" y="130" width="120" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="14" value="封禁时长" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=11;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="380" y="130" width="100" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="15" value="状态" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=11;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="480" y="130" width="80" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="16" value="操作" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=11;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="560" y="130" width="170" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="17" value="" style="rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="180" y="165" width="550" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="18" value="Player_001" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=10;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="180" y="165" width="80" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="19" value="飞行作弊" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=10;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="260" y="165" width="120" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="20" value="永久" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=10;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="380" y="165" width="100" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="21" value="生效中" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=10;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="480" y="165" width="80" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="22" value="解封 | 详情" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=10;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="560" y="165" width="170" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="23" value="" style="rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="180" y="200" width="550" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="24" value="Player_002" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=10;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="180" y="200" width="80" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="25" value="加速作弊" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=10;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="260" y="200" width="120" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="26" value="7天" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=10;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="380" y="200" width="100" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="27" value="已过期" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=10;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="480" y="200" width="80" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="28" value="详情" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=10;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="560" y="200" width="170" height="35" as="geometry"/>
        </mxCell>
      </root>
    </mxGraphModel>
  </diagram>
</mxfile>'''

def create_figure_5_7():
    return '''<mxfile host="app.diagrams.net" agent="Mozilla/5.0" version="21.0.0">
  <diagram id="diagram1" name="Page-1">
    <mxGraphModel dx="800" dy="600" grid="1" gridSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="1" pageScale="1" pageWidth="827" pageHeight="1169" math="0" shadow="0" background="#ffffff">
      <root>
        <mxCell id="0"/>
        <mxCell id="1" parent="0"/>
        <mxCell id="2" value="" style="rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="50" y="30" width="700" height="450" as="geometry"/>
        </mxCell>
        <mxCell id="3" value="Minecraft反作弊系统 - 举报管理" style="rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontStyle=1;fontSize=14;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="50" y="30" width="700" height="40" as="geometry"/>
        </mxCell>
        <mxCell id="4" value="" style="rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="50" y="70" width="120" height="410" as="geometry"/>
        </mxCell>
        <mxCell id="5" value="仪表盘" style="text;html=1;strokeColor=none;fillColor=none;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=11;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="60" y="80" width="100" height="30" as="geometry"/>
        </mxCell>
        <mxCell id="6" value="玩家管理" style="text;html=1;strokeColor=none;fillColor=none;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=11;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="60" y="115" width="100" height="30" as="geometry"/>
        </mxCell>
        <mxCell id="7" value="封禁管理" style="text;html=1;strokeColor=none;fillColor=none;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=11;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="60" y="150" width="100" height="30" as="geometry"/>
        </mxCell>
        <mxCell id="8" value="举报管理" style="text;html=1;strokeColor=none;fillColor=none;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=11;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="60" y="185" width="100" height="30" as="geometry"/>
        </mxCell>
        <mxCell id="9" value="白名单" style="text;html=1;strokeColor=none;fillColor=none;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=11;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="60" y="220" width="100" height="30" as="geometry"/>
        </mxCell>
        <mxCell id="10" value="待处理 (23)" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontSize=10;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="180" y="80" width="80" height="25" as="geometry"/>
        </mxCell>
        <mxCell id="11" value="已处理" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontSize=10;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="270" y="80" width="70" height="25" as="geometry"/>
        </mxCell>
        <mxCell id="12" value="" style="rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="180" y="120" width="550" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="13" value="举报人" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=11;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="180" y="120" width="80" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="14" value="被举报人" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=11;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="260" y="120" width="80" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="15" value="举报原因" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=11;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="340" y="120" width="120" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="16" value="时间" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=11;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="460" y="120" width="100" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="17" value="操作" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=11;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="560" y="120" width="170" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="18" value="" style="rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="180" y="155" width="550" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="19" value="Player_A" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=10;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="180" y="155" width="80" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="20" value="Player_B" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=10;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="260" y="155" width="80" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="21" value="疑似飞行作弊" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=10;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="340" y="155" width="120" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="22" value="2026-04-18" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=10;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="460" y="155" width="100" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="23" value="处理 | 驳回 | 详情" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=10;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="560" y="155" width="170" height="35" as="geometry"/>
        </mxCell>
      </root>
    </mxGraphModel>
  </diagram>
</mxfile>'''

def create_figure_5_8():
    return '''<mxfile host="app.diagrams.net" agent="Mozilla/5.0" version="21.0.0">
  <diagram id="diagram1" name="Page-1">
    <mxGraphModel dx="800" dy="600" grid="1" gridSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="1" pageScale="1" pageWidth="827" pageHeight="1169" math="0" shadow="0" background="#ffffff">
      <root>
        <mxCell id="0"/>
        <mxCell id="1" parent="0"/>
        <mxCell id="2" value="" style="rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="50" y="30" width="700" height="450" as="geometry"/>
        </mxCell>
        <mxCell id="3" value="Minecraft反作弊系统 - 白名单管理" style="rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontStyle=1;fontSize=14;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="50" y="30" width="700" height="40" as="geometry"/>
        </mxCell>
        <mxCell id="4" value="" style="rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="50" y="70" width="120" height="410" as="geometry"/>
        </mxCell>
        <mxCell id="5" value="仪表盘" style="text;html=1;strokeColor=none;fillColor=none;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=11;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="60" y="80" width="100" height="30" as="geometry"/>
        </mxCell>
        <mxCell id="6" value="玩家管理" style="text;html=1;strokeColor=none;fillColor=none;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=11;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="60" y="115" width="100" height="30" as="geometry"/>
        </mxCell>
        <mxCell id="7" value="封禁管理" style="text;html=1;strokeColor=none;fillColor=none;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=11;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="60" y="150" width="100" height="30" as="geometry"/>
        </mxCell>
        <mxCell id="8" value="举报管理" style="text;html=1;strokeColor=none;fillColor=none;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=11;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="60" y="185" width="100" height="30" as="geometry"/>
        </mxCell>
        <mxCell id="9" value="白名单" style="text;html=1;strokeColor=none;fillColor=none;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=11;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="60" y="220" width="100" height="30" as="geometry"/>
        </mxCell>
        <mxCell id="10" value="+ 添加白名单" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontSize=11;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="180" y="80" width="110" height="30" as="geometry"/>
        </mxCell>
        <mxCell id="11" value="" style="rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="180" y="130" width="550" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="12" value="玩家" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=11;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="180" y="130" width="100" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="13" value="添加原因" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=11;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="280" y="130" width="150" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="14" value="添加人" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=11;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="430" y="130" width="100" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="15" value="添加时间" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=11;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="530" y="130" width="100" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="16" value="操作" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=11;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="630" y="130" width="100" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="17" value="" style="rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="180" y="165" width="550" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="18" value="VIP_Player" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=10;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="180" y="165" width="100" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="19" value="服务器管理员" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=10;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="280" y="165" width="150" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="20" value="Admin_001" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=10;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="430" y="165" width="100" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="21" value="2026-01-15" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=10;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="530" y="165" width="100" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="22" value="移除" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=10;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="630" y="165" width="100" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="23" value="" style="rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="180" y="200" width="550" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="24" value="Trusted_01" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=10;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="180" y="200" width="100" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="25" value="长期活跃玩家" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=10;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="280" y="200" width="150" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="26" value="Admin_002" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=10;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="430" y="200" width="100" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="27" value="2026-02-20" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=10;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="530" y="200" width="100" height="35" as="geometry"/>
        </mxCell>
        <mxCell id="28" value="移除" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=10;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="630" y="200" width="100" height="35" as="geometry"/>
        </mxCell>
      </root>
    </mxGraphModel>
  </diagram>
</mxfile>'''

def create_figure_5_9():
    return '''<mxfile host="app.diagrams.net" agent="Mozilla/5.0" version="21.0.0">
  <diagram id="diagram1" name="Page-1">
    <mxGraphModel dx="800" dy="600" grid="1" gridSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="1" pageScale="1" pageWidth="827" pageHeight="1169" math="0" shadow="0" background="#ffffff">
      <root>
        <mxCell id="0"/>
        <mxCell id="1" parent="0"/>
        <mxCell id="2" value="" style="rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="50" y="30" width="700" height="450" as="geometry"/>
        </mxCell>
        <mxCell id="3" value="Minecraft反作弊系统 - 数据统计" style="rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontStyle=1;fontSize=14;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="50" y="30" width="700" height="40" as="geometry"/>
        </mxCell>
        <mxCell id="4" value="" style="rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="50" y="70" width="120" height="410" as="geometry"/>
        </mxCell>
        <mxCell id="5" value="作弊检测统计" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontSize=11;verticalAlign=top;align=left;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="180" y="80" width="270" height="180" as="geometry"/>
        </mxCell>
        <mxCell id="6" value="封禁趋势" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontSize=11;verticalAlign=top;align=left;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="470" y="80" width="260" height="180" as="geometry"/>
        </mxCell>
        <mxCell id="7" value="举报类型分布" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontSize=11;verticalAlign=top;align=left;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="180" y="280" width="270" height="180" as="geometry"/>
        </mxCell>
        <mxCell id="8" value="玩家活跃度" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontSize=11;verticalAlign=top;align=left;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="470" y="280" width="260" height="180" as="geometry"/>
        </mxCell>
      </root>
    </mxGraphModel>
  </diagram>
</mxfile>'''

def main():
    if not os.path.exists(OUTPUT_DIR):
        os.makedirs(OUTPUT_DIR)
    
    figures = [
        ('图3-1_系统功能模块层次图.drawio', create_figure_3_1),
        ('图3-2_系统用例图.drawio', create_figure_3_2),
        ('图3-3_系统顶层数据流图.drawio', create_figure_3_3),
        ('图3-4_系统0层数据流图.drawio', create_figure_3_4),
        ('图4-1_系统架构图.drawio', create_figure_4_1),
        ('图4-2_系统功能流程图.drawio', create_figure_4_2),
        ('图4-3_作弊检测业务流程图.drawio', create_figure_4_3),
        ('图4-4_举报处理业务流程图.drawio', create_figure_4_4),
        ('图4-5_系统总体E-R图.drawio', create_figure_4_5),
        ('图4-6_玩家实体E-R图.drawio', create_figure_4_6),
        ('图4-7_作弊记录实体E-R图.drawio', create_figure_4_7),
        ('图4-8_封禁记录实体E-R图.drawio', create_figure_4_8),
        ('图4-9_举报记录实体E-R图.drawio', create_figure_4_9),
        ('图4-10_管理员实体E-R图.drawio', create_figure_4_10),
        ('图4-11_白名单实体E-R图.drawio', create_figure_4_11),
        ('图5-1_用户登录界面.drawio', create_figure_5_1),
        ('图5-2_用户登录核心代码.drawio', create_figure_5_2),
        ('图5-3_玩家管理界面.drawio', create_figure_5_3),
        ('图5-4_仪表盘界面.drawio', create_figure_5_4),
        ('图5-5_WebSocket处理器核心代码.drawio', create_figure_5_5),
        ('图5-6_封禁管理界面.drawio', create_figure_5_6),
        ('图5-7_举报管理界面.drawio', create_figure_5_7),
        ('图5-8_白名单管理界面.drawio', create_figure_5_8),
        ('图5-9_数据统计图表.drawio', create_figure_5_9),
    ]
    
    for filename, create_func in figures:
        filepath = os.path.join(OUTPUT_DIR, filename)
        xml_content = create_func()
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(xml_content)
        print(f'已生成: {filepath}')
    
    print(f'\n共生成 {len(figures)} 个XML格式图表文件')

if __name__ == '__main__':
    main()
