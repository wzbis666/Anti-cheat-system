# -*- coding: utf-8 -*-
import os

OUTPUT_DIR = 'docs/figures'

def create_dfd_level0():
    return '''<mxfile host="app.diagrams.net" agent="Mozilla/5.0" version="21.0.0">
  <diagram id="dfd-level0" name="系统0层数据流图">
    <mxGraphModel dx="1200" dy="800" grid="1" gridSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="1" pageScale="1" pageWidth="1169" pageHeight="827" math="0" shadow="0" background="#ffffff">
      <root>
        <mxCell id="0"/>
        <mxCell id="1" parent="0"/>
        
        <mxCell id="title" value="图3-4 系统0层数据流图" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=14;fontStyle=1;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="450" y="20" width="200" height="30" as="geometry"/>
        </mxCell>
        
        <mxCell id="admin" value="管理员" style="rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontStyle=1;fontSize=12;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="40" y="200" width="80" height="50" as="geometry"/>
        </mxCell>
        
        <mxCell id="player" value="MC玩家" style="rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontStyle=1;fontSize=12;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="40" y="400" width="80" height="50" as="geometry"/>
        </mxCell>
        
        <mxCell id="game_server" value="游戏服务器" style="rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontStyle=1;fontSize=12;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="40" y="600" width="80" height="50" as="geometry"/>
        </mxCell>
        
        <mxCell id="system" value="Minecraft反作弊系统" style="rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontStyle=1;fontSize=14;fontColor=#000000;verticalAlign=top;" vertex="1" parent="1">
          <mxGeometry x="250" y="80" width="500" height="620" as="geometry"/>
        </mxCell>
        
        <mxCell id="p1" value="1.0&#xa;用户认证" style="ellipse;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontStyle=1;fontSize=11;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="300" y="120" width="80" height="60" as="geometry"/>
        </mxCell>
        
        <mxCell id="p2" value="2.0&#xa;玩家管理" style="ellipse;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontStyle=1;fontSize=11;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="300" y="220" width="80" height="60" as="geometry"/>
        </mxCell>
        
        <mxCell id="p3" value="3.0&#xa;作弊检测" style="ellipse;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontStyle=1;fontSize=11;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="300" y="320" width="80" height="60" as="geometry"/>
        </mxCell>
        
        <mxCell id="p4" value="4.0&#xa;封禁管理" style="ellipse;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontStyle=1;fontSize=11;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="300" y="420" width="80" height="60" as="geometry"/>
        </mxCell>
        
        <mxCell id="p5" value="5.0&#xa;举报处理" style="ellipse;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontStyle=1;fontSize=11;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="300" y="520" width="80" height="60" as="geometry"/>
        </mxCell>
        
        <mxCell id="p6" value="6.0&#xa;白名单管理" style="ellipse;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontStyle=1;fontSize=11;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="300" y="620" width="80" height="60" as="geometry"/>
        </mxCell>
        
        <mxCell id="p7" value="7.0&#xa;数据统计" style="ellipse;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontStyle=1;fontSize=11;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="500" y="220" width="80" height="60" as="geometry"/>
        </mxCell>
        
        <mxCell id="p8" value="8.0&#xa;系统设置" style="ellipse;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontStyle=1;fontSize=11;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="500" y="320" width="80" height="60" as="geometry"/>
        </mxCell>
        
        <mxCell id="d1" value="D1 用户信息" style="shape=parallelogram;perimeter=parallelogramPerimeter;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontSize=10;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="500" y="120" width="100" height="40" as="geometry"/>
        </mxCell>
        
        <mxCell id="d2" value="D2 玩家信息" style="shape=parallelogram;perimeter=parallelogramPerimeter;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontSize=10;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="500" y="420" width="100" height="40" as="geometry"/>
        </mxCell>
        
        <mxCell id="d3" value="D3 作弊记录" style="shape=parallelogram;perimeter=parallelogramPerimeter;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontSize=10;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="500" y="480" width="100" height="40" as="geometry"/>
        </mxCell>
        
        <mxCell id="d4" value="D4 封禁记录" style="shape=parallelogram;perimeter=parallelogramPerimeter;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontSize=10;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="500" y="540" width="100" height="40" as="geometry"/>
        </mxCell>
        
        <mxCell id="d5" value="D5 举报记录" style="shape=parallelogram;perimeter=parallelogramPerimeter;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontSize=10;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="500" y="600" width="100" height="40" as="geometry"/>
        </mxCell>
        
        <mxCell id="d6" value="D6 白名单" style="shape=parallelogram;perimeter=parallelogramPerimeter;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontSize=10;fontColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="500" y="660" width="100" height="40" as="geometry"/>
        </mxCell>
        
        <mxCell id="f1" value="登录信息" style="endArrow=classic;html=1;strokeColor=#000000;fontSize=10;fontColor=#000000;" edge="1" parent="1" source="admin" target="p1">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        
        <mxCell id="f2" value="认证结果" style="endArrow=classic;html=1;strokeColor=#000000;fontSize=10;fontColor=#000000;" edge="1" parent="1" source="p1" target="admin">
          <mxGeometry relative="1" as="geometry">
            <Array as="points">
              <mxPoint x="200" y="150"/>
            </Array>
          </mxGeometry>
        </mxCell>
        
        <mxCell id="f3" value="管理操作" style="endArrow=classic;html=1;strokeColor=#000000;fontSize=10;fontColor=#000000;" edge="1" parent="1" source="admin" target="p2">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        
        <mxCell id="f4" value="玩家数据" style="endArrow=classic;html=1;strokeColor=#000000;fontSize=10;fontColor=#000000;" edge="1" parent="1" source="p2" target="admin">
          <mxGeometry relative="1" as="geometry">
            <Array as="points">
              <mxPoint x="200" y="250"/>
            </Array>
          </mxGeometry>
        </mxCell>
        
        <mxCell id="f5" value="作弊数据" style="endArrow=classic;html=1;strokeColor=#000000;fontSize=10;fontColor=#000000;" edge="1" parent="1" source="game_server" target="p3">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        
        <mxCell id="f6" value="检测结果" style="endArrow=classic;html=1;strokeColor=#000000;fontSize=10;fontColor=#000000;" edge="1" parent="1" source="p3" target="admin">
          <mxGeometry relative="1" as="geometry">
            <Array as="points">
              <mxPoint x="200" y="350"/>
            </Array>
          </mxGeometry>
        </mxCell>
        
        <mxCell id="f7" value="封禁操作" style="endArrow=classic;html=1;strokeColor=#000000;fontSize=10;fontColor=#000000;" edge="1" parent="1" source="admin" target="p4">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        
        <mxCell id="f8" value="封禁状态" style="endArrow=classic;html=1;strokeColor=#000000;fontSize=10;fontColor=#000000;" edge="1" parent="1" source="p4" target="game_server">
          <mxGeometry relative="1" as="geometry">
            <Array as="points">
              <mxPoint x="200" y="450"/>
              <mxPoint x="200" y="625"/>
            </Array>
          </mxGeometry>
        </mxCell>
        
        <mxCell id="f9" value="举报信息" style="endArrow=classic;html=1;strokeColor=#000000;fontSize=10;fontColor=#000000;" edge="1" parent="1" source="player" target="p5">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        
        <mxCell id="f10" value="处理结果" style="endArrow=classic;html=1;strokeColor=#000000;fontSize=10;fontColor=#000000;" edge="1" parent="1" source="p5" target="player">
          <mxGeometry relative="1" as="geometry">
            <Array as="points">
              <mxPoint x="200" y="550"/>
            </Array>
          </mxGeometry>
        </mxCell>
        
        <mxCell id="f11" value="白名单操作" style="endArrow=classic;html=1;strokeColor=#000000;fontSize=10;fontColor=#000000;" edge="1" parent="1" source="admin" target="p6">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        
        <mxCell id="f12" value="统计请求" style="endArrow=classic;html=1;strokeColor=#000000;fontSize=10;fontColor=#000000;" edge="1" parent="1" source="admin" target="p7">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        
        <mxCell id="f13" value="统计数据" style="endArrow=classic;html=1;strokeColor=#000000;fontSize=10;fontColor=#000000;" edge="1" parent="1" source="p7" target="admin">
          <mxGeometry relative="1" as="geometry">
            <Array as="points">
              <mxPoint x="650" y="225"/>
              <mxPoint x="780" y="225"/>
            </Array>
          </mxGeometry>
        </mxCell>
        
        <mxCell id="f14" value="设置参数" style="endArrow=classic;html=1;strokeColor=#000000;fontSize=10;fontColor=#000000;" edge="1" parent="1" source="admin" target="p8">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        
        <mxCell id="f15" value="" style="endArrow=classic;html=1;strokeColor=#000000;fontSize=10;fontColor=#000000;" edge="1" parent="1" source="p1" target="d1">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        
        <mxCell id="f16" value="" style="endArrow=classic;html=1;strokeColor=#000000;fontSize=10;fontColor=#000000;" edge="1" parent="1" source="p2" target="d2">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        
        <mxCell id="f17" value="" style="endArrow=classic;html=1;strokeColor=#000000;fontSize=10;fontColor=#000000;" edge="1" parent="1" source="p3" target="d3">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        
        <mxCell id="f18" value="" style="endArrow=classic;html=1;strokeColor=#000000;fontSize=10;fontColor=#000000;" edge="1" parent="1" source="p4" target="d4">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        
        <mxCell id="f19" value="" style="endArrow=classic;html=1;strokeColor=#000000;fontSize=10;fontColor=#000000;" edge="1" parent="1" source="p5" target="d5">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        
        <mxCell id="f20" value="" style="endArrow=classic;html=1;strokeColor=#000000;fontSize=10;fontColor=#000000;" edge="1" parent="1" source="p6" target="d6">
          <mxGeometry relative="1" as="geometry"/>
        </mxCell>
        
      </root>
    </mxGraphModel>
  </diagram>
</mxfile>'''

def main():
    if not os.path.exists(OUTPUT_DIR):
        os.makedirs(OUTPUT_DIR)
    
    filepath = os.path.join(OUTPUT_DIR, '图3-4_系统0层数据流图.drawio')
    xml_content = create_dfd_level0()
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(xml_content)
    print(f'已生成: {filepath}')
    print(f'完整路径: {os.path.abspath(filepath)}')

if __name__ == '__main__':
    main()
