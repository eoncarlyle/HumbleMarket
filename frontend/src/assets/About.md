---
title: Introducing Signals
date: 2022-09-06
authors:
  - Marvin Hagemeister
  - Jason Miller
---

Signals are a way of expressing state that ensure apps stay fast regardless of how complex they get. Signals are based on reactive principles and provide excellent developer ergonomics, with a unique implementation optimized for Virtual DOM.

At its core, a signal is an object with a `.value` property that holds some value. Accessing a signal's value property from within a component automatically updates that component when the value of that signal changes.

In addition to being straightforward and easy to write, this also ensures state updates stay fast regardless of how many components your app has. Signals are fast by default, automatically optimizing updates behind the scenes for you.
