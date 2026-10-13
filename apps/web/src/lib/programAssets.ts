import courseFullstackImg from '@/assets/course-fullstack.jpg'
import courseDatascienceImg from '@/assets/course-datascience.jpg'
import courseUiuxImg from '@/assets/course-uiux.jpg'
import courseMarketingImg from '@/assets/course-marketing.jpg'

export const PROGRAM_ASSETS_MAP: Record<string, string> = {
  'course-fullstack': courseFullstackImg,
  'course-datascience': courseDatascienceImg,
  'course-uiux': courseUiuxImg,
  'course-marketing': courseMarketingImg,
}

export function resolveProgramImage(assetKeyOrUrl?: string | null): string {
  if (!assetKeyOrUrl) {
    return courseFullstackImg
  }
  if (PROGRAM_ASSETS_MAP[assetKeyOrUrl]) {
    return PROGRAM_ASSETS_MAP[assetKeyOrUrl]
  }
  // Support direct URLs or fallbacks
  if (assetKeyOrUrl.startsWith('http://') || assetKeyOrUrl.startsWith('https://') || assetKeyOrUrl.startsWith('/')) {
    return assetKeyOrUrl
  }
  return courseFullstackImg
}
