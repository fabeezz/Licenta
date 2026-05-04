## Brand & Style

The brand identity focuses on "Digital Tailoring"—a premium, high-end experience that transforms a utility app into a personal fashion concierge. The target audience is style-conscious individuals who value efficiency and high-fidelity aesthetics.

The design system employs a **Premium Apple/iOS Minimalist** style. It leverages high-quality imagery against a canvas of generous whitespace to create a "Gallery" feel. This is augmented by **Subtle Glassmorphism** to provide a sense of physical depth and sophisticated layering, ensuring the interface feels light, airy, and inherently modern.

## Colors

This design system utilizes a high-contrast monochromatic base to ensure that the colors within the clothing photography remain the focal point.

- **Base Colors:** Pure white is used for the primary canvas, with off-white and very light gray used for container backgrounds to define structure without heavy borders.
- **Accent:** A deep "Midnight Blue" is reserved for high-priority interactive states or subtle brand moments, providing a more premium feel than standard black.
- **System Colors:** Follow standard iOS semantic naming for success, error, and warning, but desaturated to maintain the minimalist aesthetic.

## Typography

Using **Inter**, the typography balances technical precision with high readability.

The hierarchy is dominated by large, bold navigation titles that are strictly left-aligned, mimicking the iOS "Large Title" behavior. Tracking (letter spacing) is tightened for larger headlines to maintain a compact, premium look. For lowercase labels and auxiliary information, a slightly increased tracking is used to ensure legibility against light backgrounds.

## Layout & Spacing

The layout philosophy follows a **Dynamic Padding** model. Instead of a rigid grid, elements are spaced relative to the page margins (20px) to provide a "breathable" interface.

Clothing items are presented in a two-column or three-column layout with minimal gutters to allow the images to occupy as much screen real estate as possible. Vertical rhythm is established using 8px increments, with larger 32px gaps used to separate distinct functional sections (e.g., separating "Recently Worn" from "Daily Suggestions").

## Elevation & Depth

Depth is communicated through **Background Contrast** and **Backdrop Blurs** rather than traditional drop shadows.

- **Level 0 (Canvas):** Pure white background.
- **Level 1 (Cards/Containers):** Light gray (#F2F2F7) with no shadow, or a white card with a 1px soft gray border.
- **Level 2 (Floating Navigation):** Floating elements like the bottom tab bar use a heavy backdrop blur (Glassmorphism) with a ultra-fine 0.5px border.
- **Level 3 (Modals/Overlays):** These utilize a diffused shadow (Blur: 30px, Opacity: 8%, Color: Black) to create a soft "lift" from the main surface.

## Shapes

The shape language is defined by **Highly Rounded Corners**, creating a friendly yet sophisticated silhouette.

Standard cards and clothing containers use a 18px-24px radius. Smaller interactive elements like chips or input fields use a 12px radius. The extreme roundedness is intended to soften the technical nature of "AI" and make the app feel more organic and fashion-forward.

## Components

- **Clothing Hero Cards:** Background-less images of clothing pieces centered on light gray (#F9F9F9) rounded tiles. No borders; use spacing to define the grid.
- **Primary Action Button:** Large, black (monochrome) button with 16px-20px corner radius and white text.
- **Floating Tab Bar:** A pill-shaped container using backdrop blur (saturate 150%, blur 20px) with a subtle shadow and centered icons.
- **Segmented Controls:** Soft gray background with a white "sliding" pill to indicate the active state (Pieces, Fits, Collections).
- **Interactive Chips:** Pill-shaped filters with a light stroke when inactive and a solid black fill when active.
- **Input Fields:** Minimalist containers with subtle gray backgrounds and no borders, focusing on the placeholder typography.
