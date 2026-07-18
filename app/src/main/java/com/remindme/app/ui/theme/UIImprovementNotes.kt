package com.remindme.app.ui.theme

// UI/UX Improvements Required:
//
// 1. OPACITY & COLORS
// - Increase opacity of popups/panels/selectors (currently too low, hard to see)
// - Add better color hierarchy: primary, secondary, tertiary
// - Fix component background colors to match theme
// - Improve contrast ratios for WCAG compliance
//
// 2. SHAPES & ROUNDING
// - Review all border radius values (24dp for buttons, 16dp for fields, etc.)
// - iOS-style rounding: use 12-16dp for most components
// - Create consistent shape system
//
// 3. LOADING STATES
// - Remove initial spinner on app launch
// - Show UI skeleton/placeholder while loading
// - Add proper loading indicators only for long operations
//
// 4. DYNAMIC FAB (Floating Action Button)
// - When clicked from Home: show menu with options (Task, Person, Subscription)
// - When clicked from People: directly add Person
// - When clicked from Tasks: directly add Task
// - When clicked from Subscriptions: directly add Subscription
//
// 5. THEME SWITCHER
// - Implement real blur theme (current): use Modifier.blur()
// - Create solid theme (new): flat colors without blur
// - Store selection in DataStore (done)
// - Apply at app startup
//
// 6. FONT HIERARCHY
// - H1 (32sp, Bold): Page titles
// - H2 (24sp, SemiBold): Section headers
// - H3 (18sp, SemiBold): Subsection headers
// - Body (16sp, Regular): Regular text
// - Caption (12sp, Regular): Secondary text
// - Small (10sp, Regular): Tertiary text
//
// 7. ICON COLORS
// - Primary icons: match primary brand color
// - Secondary icons: 60% opacity
// - Disabled icons: 30% opacity
//
// 8. COMPONENT IMPROVEMENTS
// - TextField: increase background opacity, better placeholder styling
// - Button: consistent padding (16.dp horizontal, 12.dp vertical)
// - Card: proper elevation/shadow
// - Menu: better spacing, icons + text alignment
//
// 9. GLITCHY/INCONSISTENT ISSUES
// - Review all Material3 color tokens
// - Ensure consistent spacing throughout app
// - Check padding/margin consistency (use 4.dp, 8.dp, 12.dp, 16.dp, 24.dp scale)
// - Fix any hardcoded colors
