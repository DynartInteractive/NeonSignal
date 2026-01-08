<?xml version="1.0" encoding="UTF-8"?>
<tileset version="1.10" tiledversion="1.11.2" name="objects" tilewidth="92" tileheight="64" tilecount="33" columns="0">
 <grid orientation="orthogonal" width="32" height="32"/>
 <tile id="0">
  <properties>
   <property name="type" value="player"/>
  </properties>
  <image source="objects/player.png" width="32" height="32"/>
 </tile>
 <tile id="1">
  <properties>
   <property name="direction" value="left"/>
   <property name="group" value=""/>
   <property name="speed" type="float" value="28"/>
   <property name="type" value="crab"/>
   <property name="watch_edge" type="bool" value="true"/>
  </properties>
  <image source="objects/crab.png" width="32" height="17"/>
 </tile>
 <tile id="2">
  <properties>
   <property name="active" type="bool" value="false"/>
   <property name="gravity" type="float" value="-700"/>
   <property name="group" value=""/>
   <property name="start_speed" type="float" value="420"/>
   <property name="type" value="piranha"/>
   <property name="wait" type="float" value="1"/>
  </properties>
  <image source="objects/piranha.png" width="22" height="30"/>
 </tile>
 <tile id="3">
  <properties>
   <property name="active" type="bool" value="false"/>
   <property name="group" value=""/>
   <property name="move_type" value="fc"/>
   <property name="path" value=""/>
   <property name="slowing" type="bool" value="true"/>
   <property name="speed" type="float" value="60"/>
   <property name="start_index" type="int" value="0"/>
   <property name="type" value="watermine"/>
   <property name="wait_time" type="float" value="0"/>
  </properties>
  <image source="objects/watermine.png" width="32" height="32"/>
 </tile>
 <tile id="4">
  <properties>
   <property name="item" value=""/>
   <property name="type" value="box"/>
  </properties>
  <image source="objects/box.png" width="16" height="16"/>
 </tile>
 <tile id="5">
  <properties>
   <property name="type" value="floppy"/>
  </properties>
  <image source="objects/floppy.png" width="16" height="16"/>
 </tile>
 <tile id="6">
  <properties>
   <property name="type" value="lollipop"/>
  </properties>
  <image source="objects/lollipop.png" width="16" height="16"/>
 </tile>
 <tile id="7">
  <properties>
   <property name="type" value="tequila"/>
  </properties>
  <image source="objects/tequila.png" width="16" height="16"/>
 </tile>
 <tile id="8">
  <properties>
   <property name="active" type="bool" value="false"/>
   <property name="group" value=""/>
   <property name="move_type" value="fc"/>
   <property name="offset" type="int" value="1"/>
   <property name="path" value=""/>
   <property name="slowing" type="bool" value="true"/>
   <property name="speed" type="float" value="0"/>
   <property name="sprite" value="platform1"/>
   <property name="start_index" type="int" value="0"/>
   <property name="type" value="platform"/>
   <property name="wait_time" type="float" value="0"/>
  </properties>
  <image source="objects/platform1.png" width="31" height="8"/>
 </tile>
 <tile id="9">
  <properties>
   <property name="delay" type="float" value="0.33"/>
   <property name="distance" type="float" value="64"/>
   <property name="group" value=""/>
   <property name="type" value="coconut"/>
  </properties>
  <image source="objects/coconut.png" width="12" height="17"/>
 </tile>
 <tile id="10">
  <properties>
   <property name="type" value="spike"/>
  </properties>
  <image source="objects/spike.png" width="16" height="12"/>
 </tile>
 <tile id="11">
  <properties>
   <property name="type" value="falling_platform"/>
  </properties>
  <image source="objects/falling_floor1.png" width="16" height="8"/>
 </tile>
 <tile id="12">
  <properties>
   <property name="active" type="bool" value="false"/>
   <property name="direction" value="left"/>
   <property name="group" value=""/>
   <property name="speed" type="float" value="20"/>
   <property name="type" value="hedgehog"/>
   <property name="watch_edge" type="bool" value="true"/>
  </properties>
  <image source="objects/hedgehog.png" width="25" height="14"/>
 </tile>
 <tile id="13">
  <properties>
   <property name="type" value="dplatform"/>
  </properties>
  <image source="objects/dplatform.png" width="16" height="16"/>
 </tile>
 <tile id="14">
  <properties>
   <property name="active" type="bool" value="false"/>
   <property name="direction" value="left"/>
   <property name="forward_speed" type="float" value="100"/>
   <property name="group" value=""/>
   <property name="jump_speed" type="float" value="200"/>
   <property name="type" value="frog"/>
   <property name="wait" type="float" value="0.6"/>
  </properties>
  <image source="objects/frog.png" width="22" height="15"/>
 </tile>
 <tile id="15">
  <properties>
   <property name="type" value="tram"/>
  </properties>
  <image source="objects/tram.png" width="24" height="17"/>
 </tile>
 <tile id="17">
  <properties>
   <property name="jump_speed" type="float" value="600"/>
   <property name="type" value="springboard"/>
  </properties>
  <image source="objects/springboard.png" width="20" height="13"/>
 </tile>
 <tile id="18">
  <properties>
   <property name="active" type="bool" value="false"/>
   <property name="move_type" value="fc"/>
   <property name="path" value=""/>
   <property name="slowing" type="bool" value="true"/>
   <property name="speed" type="float" value="60"/>
   <property name="sprite" value="movable1"/>
   <property name="start" type="bool" value="false"/>
   <property name="start_index" type="int" value="0"/>
   <property name="type" value="movable"/>
   <property name="wait_time" type="float" value="0"/>
  </properties>
  <image source="objects/movable1.png" width="49" height="16"/>
 </tile>
 <tile id="19">
  <properties>
   <property name="inverse" type="bool" value="false"/>
   <property name="names" value=""/>
   <property name="repeat_time" type="float" value="0"/>
   <property name="type" value="button"/>
  </properties>
  <image source="objects/button1.png" width="16" height="5"/>
 </tile>
 <tile id="20">
  <properties>
   <property name="type" value="oxygen"/>
  </properties>
  <image source="objects/oxygen.png" width="32" height="32"/>
 </tile>
 <tile id="21">
  <properties>
   <property name="left" type="bool" value="false"/>
   <property name="type" value="exit"/>
  </properties>
  <image source="objects/exit.png" width="16" height="16"/>
 </tile>
 <tile id="22">
  <properties>
   <property name="cutscene" type="bool" value="false"/>
   <property name="once" type="bool" value="false"/>
   <property name="path" value=""/>
   <property name="type" value="action"/>
  </properties>
  <image source="objects/action.png" width="16" height="16"/>
 </tile>
 <tile id="23">
  <properties>
   <property name="names" value=""/>
   <property name="type" value="kill_switch"/>
  </properties>
  <image source="objects/kill_switch.png" width="16" height="16"/>
 </tile>
 <tile id="24">
  <properties>
   <property name="bottom" value=""/>
   <property name="fade" type="bool" value="false"/>
   <property name="instant" type="bool" value="false"/>
   <property name="left" value=""/>
   <property name="right" value=""/>
   <property name="top" value=""/>
   <property name="type" value="camera_limit_trigger"/>
  </properties>
  <image source="objects/camera_limit_trigger.png" width="16" height="16"/>
 </tile>
 <tile id="25">
  <properties>
   <property name="type" value="revive"/>
  </properties>
  <image source="objects/revive.png" width="16" height="16"/>
 </tile>
 <tile id="26">
  <properties>
   <property name="type" value="target"/>
  </properties>
  <image source="objects/target.png" width="16" height="16"/>
 </tile>
 <tile id="27">
  <properties>
   <property name="active" type="bool" value="false"/>
   <property name="direction" value="left"/>
   <property name="group" value=""/>
   <property name="move_type" value="fc"/>
   <property name="path" value=""/>
   <property name="slowing" type="bool" value="true"/>
   <property name="speed" type="float" value="60"/>
   <property name="start_index" type="int" value="0"/>
   <property name="type" value="purple_piranha"/>
   <property name="wait_time" type="float" value="0"/>
  </properties>
  <image source="objects/purple_piranha.png" width="32" height="32"/>
 </tile>
 <tile id="29">
  <properties>
   <property name="active" type="bool" value="false"/>
   <property name="animation" value=""/>
   <property name="flip_x" type="bool" value="false"/>
   <property name="flip_y" type="bool" value="false"/>
   <property name="layer" type="int" value="100"/>
   <property name="move_type" value="fc"/>
   <property name="path" value=""/>
   <property name="slowing" type="bool" value="true"/>
   <property name="speed" type="float" value="60"/>
   <property name="sprite" value="barman"/>
   <property name="start" type="bool" value="false"/>
   <property name="start_index" type="int" value="0"/>
   <property name="type" value="decoration"/>
   <property name="wait_time" type="float" value="0"/>
  </properties>
  <image source="objects/barman.png" width="32" height="32"/>
 </tile>
 <tile id="30">
  <properties>
   <property name="active" type="bool" value="false"/>
   <property name="animation" value=""/>
   <property name="flip_x" type="bool" value="false"/>
   <property name="flip_y" type="bool" value="false"/>
   <property name="layer" type="int" value="100"/>
   <property name="move_type" value="fc"/>
   <property name="path" value=""/>
   <property name="slowing" type="bool" value="true"/>
   <property name="speed" type="float" value="60"/>
   <property name="sprite" value="foxgirl"/>
   <property name="start" type="bool" value="false"/>
   <property name="start_index" type="int" value="0"/>
   <property name="type" value="decoration"/>
   <property name="wait_time" type="float" value="0"/>
  </properties>
  <image source="objects/foxgirl.png" width="32" height="32"/>
 </tile>
 <tile id="28">
  <properties>
   <property name="active" type="bool" value="false"/>
   <property name="move_type" value="fc"/>
   <property name="path" value=""/>
   <property name="slowing" type="bool" value="true"/>
   <property name="speed" type="float" value="60"/>
   <property name="sprite" value="movable1_vertical"/>
   <property name="start" type="bool" value="false"/>
   <property name="start_index" type="int" value="0"/>
   <property name="type" value="movable"/>
   <property name="wait_time" type="float" value="0"/>
  </properties>
  <image source="objects/movable1_vertical.png" width="17" height="49"/>
 </tile>
 <tile id="31">
  <properties>
   <property name="active" type="bool" value="false"/>
   <property name="animation" value=""/>
   <property name="flip_x" type="bool" value="false"/>
   <property name="flip_y" type="bool" value="false"/>
   <property name="layer" type="int" value="100"/>
   <property name="move_type" value="fc"/>
   <property name="path" value=""/>
   <property name="slowing" type="bool" value="true"/>
   <property name="speed" type="float" value="60"/>
   <property name="sprite" value="ufo_beam"/>
   <property name="start" type="bool" value="false"/>
   <property name="start_index" type="int" value="0"/>
   <property name="type" value="decoration"/>
   <property name="wait_time" type="float" value="0"/>
  </properties>
  <image source="objects/ufo_beam.png" width="30" height="64"/>
 </tile>
 <tile id="100">
  <properties>
   <property name="type" value="coin"/>
  </properties>
  <image source="objects/coin.png" width="16" height="16"/>
 </tile>
 <tile id="1000">
  <properties>
   <property name="active" type="bool" value="false"/>
   <property name="move_type" value="fc"/>
   <property name="path" value=""/>
   <property name="slowing" type="bool" value="true"/>
   <property name="speed" type="float" value="60"/>
   <property name="sprite" value="ufo"/>
   <property name="start" type="bool" value="false"/>
   <property name="start_index" type="int" value="0"/>
   <property name="type" value="ufo"/>
   <property name="wait_time" type="float" value="0"/>
  </properties>
  <image source="objects/ufo.png" width="92" height="34"/>
 </tile>
</tileset>
