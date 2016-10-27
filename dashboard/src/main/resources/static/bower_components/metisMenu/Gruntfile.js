/*
 * Copyright (c) 2016 Open Baton (http://www.openbaton.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

"use strict";

module.exports = function(grunt) {
  grunt.initConfig({
    pkg: grunt.file.readJSON("package.json"),

    banner: "/*\n" +
    " * <%= pkg.title || pkg.name %> - v<%= pkg.version %>\n" +
    " * <%= pkg.description %>\n" +
    " * <%= pkg.homepage %>\n" +
    " *\n" +
    " * Made by <%= pkg.author.name %>\n" +
    " * Under <%= pkg.license %> License\n" +
    " */\n",

    jshint: {
      options: {
        jshintrc: ".jshintrc"
      },
      all: [
      "Gruntfile.js",
      "src/metisMenu.js"
      ]
    },
    concat: {
      plugin: {
        src: ["src/metisMenu.js"],
        dest: "dist/metisMenu.js"
      },
      css: {
        src: ["src/metisMenu.css"],
        dest: "dist/metisMenu.css"
      },
      options: {
        banner: "<%= banner %>"
      }
    },
    uglify: {
      plugin: {
        src: ["dist/metisMenu.js"],
        dest: "dist/metisMenu.min.js"
      },
      options: {
        banner: "<%= banner %>"
      }
    },
    cssmin: {
      options: {
        banner: "<%= banner %>"
      },
      menucss: {
        src: ["src/metisMenu.css"],
        dest: "dist/metisMenu.min.css"
      }
    }
  });

  grunt.loadNpmTasks("grunt-contrib-jshint");
  grunt.loadNpmTasks("grunt-contrib-concat");
  grunt.loadNpmTasks("grunt-contrib-uglify");
  grunt.loadNpmTasks("grunt-contrib-cssmin");

  grunt.registerTask("travis", ["jshint"]);
  grunt.registerTask("default", ["jshint", "concat", "uglify", "cssmin"]);
};
